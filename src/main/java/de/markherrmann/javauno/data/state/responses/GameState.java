package de.markherrmann.javauno.data.state.responses;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;

import java.util.List;

public class GameState {

    private boolean success;
    private String message;
    private Game game;
    private List<Player> players;
    private List<Card> ownCards;

    public GameState(){

    }

    public GameState(Game game, Player player) {
        this.players = game.getPlayers();
        this.game = game;
        this.ownCards = player.getCards();
        this.success = true;
        this.message = "success";
    }

    public GameState(Exception exception) {
        this.success = false;
        this.message = "failure: " + exception;
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

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
