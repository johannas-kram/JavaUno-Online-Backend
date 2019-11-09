package de.markherrmann.javauno.data.state.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.markherrmann.javauno.data.fixed.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {

	private String uuid;
	private String name;
	private List<Card> cards = new ArrayList<>();
	private boolean bot;
	private boolean unoSaid;
	private int take;

	public Player(String name, boolean bot){
	    this.uuid = UUID.randomUUID().toString();
		this.name = name;
		this.bot = bot;
	}


	@JsonIgnore
	public String getUuid(){
		return uuid;
	}
	
	public String getName(){
		return name;
	}

	@JsonIgnore
	public List<Card> getCards(){
		return cards;
	}

	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean bot) {
		this.bot = bot;
	}

    public boolean isUnoSaid() {
        return unoSaid;
    }

    public void setUnoSaid(boolean unoSaid) {
        this.unoSaid = unoSaid;
    }

    public int getTake() {
        return take;
    }

    public void setTake(int take) {
        this.take = take;
    }

	public int getCardCount(){
		return cards.size();
	}
}