package de.markherrmann.javauno.data.state.components;

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
	private int take;
	private Color desiredColor;
	private int currentPlayerIndex;

	public Game(){
	    this.uuid = UUID.randomUUID().toString();
	    this.currentPlayerIndex = 0;
    }

	public String getUuid(){
		return uuid;
	}
	
	public Map<String, Player> getPlayer(){
		return player;
	}

    public List<Player> getPlayerList() {
        return playerList;
    }

    public Stack<Card> getLayStack(){
		return layStack;
	}
	
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

	public int getTake() {
		return take;
	}

	public void setTake(int take) {
		this.take = take;
	}

	public boolean playersAddable() {
		return layStack.isEmpty() && takeStack.isEmpty();
	}

	public boolean gameStartable() {
		return playerList.get(currentPlayerIndex).getCards().isEmpty();
	}
}