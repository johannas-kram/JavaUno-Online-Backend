package de.markherrmann.javauno.data.state.components;

import de.markherrmann.javauno.data.fixed.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {
	
	private String uuid;
	private String name;
	private List<Card> cards = new ArrayList<>();
	private boolean bot;

	public Player(String name, boolean bot){
	    this.uuid = UUID.randomUUID().toString();
		this.name = name;
		this.bot = bot;
	}
	
	public String getUuid(){
		return uuid;
	}
	
	public String getName(){
		return name;
	}
	
	public List<Card> getCards(){
		return cards;
	}

	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean bot) {
		this.bot = bot;
	}
}