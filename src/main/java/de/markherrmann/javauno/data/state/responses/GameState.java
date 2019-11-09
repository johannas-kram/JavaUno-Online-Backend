package de.markherrmann.javauno.data.state.responses;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;

import java.util.List;

public class GameState {

    private Game game;
    private List<Player> players;
    private List<Card> ownCards;

    public GameState(Game game, Player player) {
        this.players = game.getPlayerList();
        this.game = game;
        this.ownCards = player.getCards();
    }

    public Game getGame() {
        return game;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Card> getOwnCards() {
        return ownCards;
    }
}
