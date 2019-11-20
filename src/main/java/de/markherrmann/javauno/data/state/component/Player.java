package de.markherrmann.javauno.data.state.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.markherrmann.javauno.data.fixed.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {

	private String uuid;
	private String botUuid;
	private String name;
	private List<Card> cards = new ArrayList<>();
	private int cardCount;
	private boolean bot;
	private boolean unoSaid;
	private int take;

	public Player(){

	}

	public Player(String name, boolean bot){
	    this.uuid = UUID.randomUUID().toString();
		this.name = name;
		this.bot = bot;
		if(bot){
		    botUuid = UUID.randomUUID().toString();
        }
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

	public synchronized void removeCard(Card card){
	    cards.remove(card);
    }

	public synchronized void removeCard(int index){
		cards.remove(index);
	}

    public synchronized void addCard(Card card){
        cards.add(card);
    }

    public synchronized void clearCards(){
	    cards.clear();
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

	public String getBotUuid(){
		return botUuid;
	}
}
