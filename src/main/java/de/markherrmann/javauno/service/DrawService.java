package de.markherrmann.javauno.service;

import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DrawService {

    private final TurnService turnService;
    private final PutService putService;
    private final Logger logger = LoggerFactory.getLogger(DrawService.class);

    @Autowired
    public DrawService(TurnService turnService, PutService putService){
        this.turnService = turnService;
        this.putService = putService;
    }

    public DrawnCardResponse draw(String gameUuid, String playerUuid) throws IllegalArgumentException, IllegalStateException {
        Game game = turnService.getGame(gameUuid);
        Card card;
        boolean matches;
        synchronized (game){
            Player player = turnService.getPlayer(playerUuid, game);
            preChecks(game, player);
            card = drawCard(game, player);
            matches = matchesDrawnCard(game, card);
            if(TurnState.PUT_DRAWN.equals(game.getTurnState()) && !matches){
                game.setTurnState(TurnState.FINAL_COUNTDOWN);
            }
        }
        if(TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            turnService.finalizeTurn(game);
        }
        return new DrawnCardResponse(card, matches);
    }

    private Card drawCard(Game game, Player player){
        Card card = game.getDrawPile().pop();
        player.addCard(card);
        maybeRearrangePiles(game);
        setTurnState(game, player);
        turnService.updateLastAction(game);
        logger.info(String.format(
                "Drawn card successfully. Game: %s; Player: %s; card: %s",
                game.getUuid(),
                player.getUuid(),
                card));
        return card;
    }

    private void maybeRearrangePiles(Game game){
        if(game.getDrawPile().isEmpty()){
            Card topCard = game.getDiscardPile().pop();
            game.getDrawPile().addAll(game.getDiscardPile());
            Collections.shuffle(game.getDrawPile());
            game.getDiscardPile().clear();
            game.getDiscardPile().push(topCard);
        }
    }

    private void setTurnState(Game game, Player player){
        if(TurnState.PUT_OR_DRAW.equals(game.getTurnState())){
            game.setTurnState(TurnState.PUT_DRAWN);
        } else if(TurnState.DRAW_PENALTIES.equals(game.getTurnState())) {
            handleUnoMistake(game, player);
        } else {
            handleDrawDuty(game);
        }
    }

    private void handleDrawDuty(Game game){
        setDrawDuties(game);
        if(isDrawDutyLeft(game)){
            game.setTurnState(TurnState.DRAW_DUTIES);
        } else {
            game.setTurnState(TurnState.PUT_OR_DRAW);
        }
    }

    private void handleUnoMistake(Game game, Player player){
        setDrawPenalties(player);
        if(!isDrawPenaltyLeft(player)){
            if(isDrawDutyLeft(game)){
                game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
            } else {
                game.setTurnState(TurnState.PUT_OR_DRAW);
            }
        }
    }

    private void setDrawDuties(Game game){
        int drawDuties = game.getDrawDuties()-1;
        game.setDrawDuties(drawDuties);
    }

    private void setDrawPenalties(Player player){
        int drawDuties = player.getDrawPenalties()-1;
        player.setDrawPenalties(drawDuties);
    }

    private boolean isDrawDutyLeft(Game game){
        int drawDuties = game.getDrawDuties();
        return drawDuties > 0;
    }

    private boolean isDrawPenaltyLeft(Player player){
        int drawDuties = player.getDrawPenalties();
        return drawDuties > 0;
    }

    private boolean matchesDrawnCard(Game game, Card drawn){
        if(drawn.isJokerCard()){
            return true;
        }
        return putService.isMatch(game, drawn);
    }

    private void preChecks(Game game, Player player) throws IllegalStateException {
        if(!turnService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            throw new IllegalStateException("game is in wrong lifecycle.");
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.DRAW_PENALTIES,
                TurnState.DRAW_DUTIES,
                TurnState.DRAW_DUTIES_OR_CUMULATIVE,
                TurnState.PUT_OR_DRAW);
        if(!turnService.isPlayersTurn(game, player)){
            throw new IllegalStateException("it's not your turn.");
        }
    }

}
