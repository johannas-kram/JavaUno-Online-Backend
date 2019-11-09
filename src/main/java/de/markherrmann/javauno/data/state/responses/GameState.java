package de.markherrmann.javauno.data.state.responses;

import de.markherrmann.javauno.data.state.components.Player;

import java.util.List;

public class GameState {

    private List<Player> players;

    public GameState(List<Player> players){
        this.players = players;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getStateType(){
        return this.getClass().getSimpleName();
    }
}
