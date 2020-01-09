package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.springframework.stereotype.Service;

@Service
public class FinalizeTurnService {

    private Game game;

    void finalizeTurn(Game game){
        this.game = game;
        setPlayersDrawPenalties();
        setNextPlayer();
    }

    private void setPlayersDrawPenalties(){
        Player player = game.getPlayers().get(game.getCurrentPlayerIndex());
        boolean lastCard = player.getCards().size() == 1;
        boolean saidUno = player.isUnoSaid();
        if((lastCard && !saidUno) || (!lastCard && saidUno)){
            player.setDrawPenalties(2);
        }
        player.setUnoSaid(false);
    }

    private void setNextPlayer(){
        int index = getNextPlayerIndex();
        TurnState turnState = getNextTurnState(index);
        game.setCurrentPlayerIndex(index);
        game.setTurnState(turnState);
    }

    private int getNextPlayerIndex(){
        int currentIndex = game.getCurrentPlayerIndex();
        int players = game.getPlayers().size();
        int steps = 1;
        if(game.isSkip()){
            game.setSkip(false);
            steps = 2;
        }
        if(game.isReversed()){
            steps = players - steps;
        }
        return (currentIndex + steps) % players;
    }

    private TurnState getNextTurnState(int index){
        Player player = game.getPlayers().get(index);
        if(player.getDrawPenalties() > 0){
            return TurnState.DRAW_PENALTIES;
        }
        if(game.getDrawDuties() > 0){
            return TurnState.DRAW_DUTIES_OR_CUMULATIVE;
        }
        return TurnState.PUT_OR_DRAW;
    }
}
