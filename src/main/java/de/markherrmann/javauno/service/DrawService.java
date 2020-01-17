package de.markherrmann.javauno.service;

import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import de.markherrmann.javauno.service.push.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DrawService {

    private final TurnService turnService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DrawService.class);

    @Autowired
    public DrawService(TurnService turnService){
        this.turnService = turnService;
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
        turnService.updateLastAction(game);
        turnService.pushAction(PushMessage.DRAWN_CARD, game);
        return new DrawnCardResponse(card, matches);
    }

    static Card drawCard(Game game, Player player){
        Card card = game.getDrawPile().pop();
        player.addCard(card);
        maybeRearrangePiles(game);
        setTurnState(game, player);
        LOGGER.info(
                "Drawn card successfully. Game: {}; Player: {} card: {}",
                game.getUuid(),
                player.getUuid(),
                card);
        player.setUnoSaid(false);
        return card;
    }

    private static void maybeRearrangePiles(Game game){
        if(game.getDrawPile().isEmpty()){
            Card topCard = game.getDiscardPile().pop();
            game.getDrawPile().addAll(game.getDiscardPile());
            Collections.shuffle(game.getDrawPile());
            game.getDiscardPile().clear();
            game.getDiscardPile().push(topCard);
        }
    }

    private static void setTurnState(Game game, Player player){
        if(TurnState.PUT_OR_DRAW.equals(game.getTurnState())){
            game.setTurnState(TurnState.PUT_DRAWN);
        } else if(TurnState.DRAW_PENALTIES.equals(game.getTurnState())) {
            handleUnoMistake(game, player);
        } else {
            handleDrawDuty(game);
        }
    }

    private static void handleDrawDuty(Game game){
        setDrawDuties(game);
        if(isDrawDutyLeft(game)){
            game.setTurnState(TurnState.DRAW_DUTIES);
        } else {
            game.setTurnState(TurnState.PUT_OR_DRAW);
        }
    }

    private static void handleUnoMistake(Game game, Player player){
        setDrawPenalties(player);
        if(!isDrawPenaltyLeft(player)){
            if(isDrawDutyLeft(game)){
                game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
            } else {
                game.setTurnState(TurnState.PUT_OR_DRAW);
            }
        }
    }

    private static void setDrawDuties(Game game){
        int drawDuties = game.getDrawDuties()-1;
        game.setDrawDuties(drawDuties);
    }

    private static void setDrawPenalties(Player player){
        int drawDuties = player.getDrawPenalties()-1;
        player.setDrawPenalties(drawDuties);
    }

    private static boolean isDrawDutyLeft(Game game){
        int drawDuties = game.getDrawDuties();
        return drawDuties > 0;
    }

    private static boolean isDrawPenaltyLeft(Player player){
        int drawDuties = player.getDrawPenalties();
        return drawDuties > 0;
    }

    private boolean matchesDrawnCard(Game game, Card drawn){
        return PutService.isMatch(game, drawn);
    }

    private void preChecks(Game game, Player player) throws IllegalStateException {
        if(!turnService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.DRAW_PENALTIES,
                TurnState.DRAW_DUTIES,
                TurnState.DRAW_DUTIES_OR_CUMULATIVE,
                TurnState.PUT_OR_DRAW);
        if(!turnService.isPlayersTurn(game, player)){
            throw new IllegalStateException(ExceptionMessage.NOT_YOUR_TURN.getValue());
        }
    }

}
