package de.markherrmann.javauno.data.state.responses;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;

import java.util.List;

public class GameRunningState extends GameState {

    private Game game;
    private List<Card> ownCards;

    public GameRunningState(Game game, Player player) {
        super(game.getPlayerList());
        this.game = game;
        this.ownCards = player.getCards();
    }

    public Game getGame() {
        return game;
    }

    public List<Card> getOwnCards() {
        return ownCards;
    }
}
