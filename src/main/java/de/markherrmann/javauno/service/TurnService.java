package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.springframework.stereotype.Service;

@Service
public class TurnService {

    boolean isPlayersTurn(Game game, Player player){
        int currentIndex = game.getCurrentPlayerIndex();
        Player current = game.getPlayers().get(currentIndex);
        return current.equals(player);
    }

    void failIfInvalidTurnState(Game game, TurnState... validTurnStates) throws IllegalStateException {
        for(TurnState state : validTurnStates){
            if(game.getTurnState().equals(state)){
                return;
            }
        }
        throw new IllegalStateException("Turn is in wrong state for this action.");
    }
}
