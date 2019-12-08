package de.markherrmann.javauno.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;

import java.util.List;

public class GameStateResponse extends GeneralResponse {

    private Game game;
    private List<Player> players;
    private List<Card> ownCards;

    @JsonProperty(value= "myIndex")
    private int playersIndex;

    private GameStateResponse(){}

    public GameStateResponse(Game game, Player player, int playersIndex) {
        super(true, "success");
        this.players = game.getPlayers();
        this.game = game;
        this.ownCards = player.getCards();
        this.playersIndex = playersIndex;
    }

    public GameStateResponse(Exception exception) {
        super(false, "failure: " + exception);
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

    public int getPlayersIndex() {
        return playersIndex;
    }
}
