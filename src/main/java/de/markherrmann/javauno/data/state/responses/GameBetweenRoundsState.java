package de.markherrmann.javauno.data.state.responses;

import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;

public class GameBetweenRoundsState extends GameRunningState {
    public GameBetweenRoundsState(Game game, Player player) {
        super(game, player);
    }
}
