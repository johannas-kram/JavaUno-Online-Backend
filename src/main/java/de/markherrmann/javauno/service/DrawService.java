package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DrawService {

    private final TurnService turnService;

    @Autowired
    public DrawService(TurnService turnService){
        this.turnService = turnService;
    }

    public Card draw(String gameUuid, String playerUuid) throws IllegalArgumentException {
        Game game = turnService.getGame(gameUuid);
        synchronized (game){
            Player player = turnService.getPlayer(playerUuid, game);
            preChecks(game, player);
            return drawCard(game, player);
        }
    }

    private Card drawCard(Game game, Player player){
        Card card = game.getDrawPile().pop();
        player.addCard(card);
        maybeRearrangePiles(game);
        setTurnState(game);
        turnService.updateLastAction(game);
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

    private void setTurnState(Game game){
        if(TurnState.PUT_OR_DRAW.equals(game.getTurnState())){
            game.setTurnState(TurnState.PUT_DRAWN);
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

    private void setDrawDuties(Game game){
        int drawDuties = game.getDrawDuties()-1;
        game.setDrawDuties(drawDuties);
    }

    private boolean isDrawDutyLeft(Game game){
        int drawDuties = game.getDrawDuties()-1;
        return drawDuties > 0;
    }

    private void preChecks(Game game, Player player){
        if(!turnService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            throw new IllegalStateException("game is in wrong lifecycle.");
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.DRAW_DUTIES,
                TurnState.DRAW_DUTIES_OR_CUMULATIVE,
                TurnState.PUT_OR_DRAW);
        if(!turnService.isPlayersTurn(game, player)){
            throw new IllegalStateException("it's not your turn.");
        }
    }

}
