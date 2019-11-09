package de.markherrmann.javauno.data.state.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Color;

import java.util.*;

public class Game{
	
	private String uuid;
	private Map<String, Player> player = new HashMap<>();
	private List<Player> playerList = new ArrayList<>();
	private Stack<Card> layStack = new Stack<>();
	private Stack<Card> takeStack = new Stack<>();
	private boolean reversed;
	private Color desiredColor;
	private int currentPlayerIndex;
	private GameLifecycle gameLifecycle = GameLifecycle.ADD_PLAYERS;

	public Game(){
	    this.uuid = UUID.randomUUID().toString();
	    this.currentPlayerIndex = 0;
    }

    @JsonIgnore
	public String getUuid(){
		return uuid;
	}

	@JsonIgnore
	public Map<String, Player> getPlayer(){
		return player;
	}

	@JsonIgnore
    public List<Player> getPlayerList() {
        return playerList;
    }

	@JsonIgnore
    public Stack<Card> getLayStack(){
		return layStack;
	}

	@JsonIgnore
	public Stack<Card> getTakeStack(){
		return takeStack;
	}

    public boolean isReversed() {
        return reversed;
    }

    public Color getDesiredColor(){
		return desiredColor;
	}

    public void toggleReversed(){
	    reversed = !reversed;
    }

    public void setDesiredColor(Color desiredColor) {
        this.desiredColor = desiredColor;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    @JsonIgnore
	public GameLifecycle getGameLifecycle() {
		return gameLifecycle;
	}

	public void setGameLifecycle(GameLifecycle gameLifecycle) {
		this.gameLifecycle = gameLifecycle;
	}

	@JsonIgnore
	public boolean playersAddable() {
		return layStack.isEmpty() && takeStack.isEmpty();
	}

	@JsonIgnore
	public boolean gameStartable() {
		return playerList.get(currentPlayerIndex).getCards().isEmpty();
	}

	public Card getTopCard(){
		return layStack.peek();
	}
}