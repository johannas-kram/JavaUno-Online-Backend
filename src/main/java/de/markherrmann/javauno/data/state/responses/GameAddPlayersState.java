package de.markherrmann.javauno.data.state.responses;


import de.markherrmann.javauno.data.state.components.Player;

import java.util.List;

public class GameAddPlayersState extends GameState {
    public GameAddPlayersState(List<Player> players) {
        super(players);
    }
}