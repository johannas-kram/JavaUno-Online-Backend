package de.markherrmann.javauno.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;

import java.util.List;

public class GameState {

    private boolean success;
    private String message;
    private Game game;
    private List<Player> players;
    private List<Card> ownCards;

    @JsonProperty(value= "yourTurn")
    private boolean playersTurn;

    private GameState(){}

    public GameState(Game game, Player player, boolean playersTurn) {
        this.players = game.getPlayers();
        this.game = game;
        this.ownCards = player.getCards();
        this.success = true;
        this.message = "success";
        this.playersTurn = playersTurn;
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

    public boolean isPlayersTurn() {
        return playersTurn;
    }
}
