package de.markherrmann.javauno.data.state.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.markherrmann.javauno.data.fixed.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {

	private String uuid;
	private String publicUuid;
	private String name;
	private List<Card> cards = new ArrayList<>();
	private int cardCount;
	private boolean bot;
	private boolean unoSaid;
	private int drawPenalties;
	private boolean stopPartyRequested;
	private boolean botifyPending;
	private int drawn = 0;

	private Player(){}

	public Player(String name, boolean bot){
	    this.uuid = UUID.randomUUID().toString();
		this.name = name;
		this.bot = bot;
		publicUuid = UUID.randomUUID().toString();
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

	public void removeCard(Card card){
	    cards.remove(card);
    }

	public void removeCard(int index){
		cards.remove(index);
	}

    public void addCard(Card card){
        cards.add(card);
    }

    public void clearCards(){
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

	public int getCardCount(){
		return cards.size();
	}

	public String getPublicUuid(){
		return publicUuid;
	}

	public void setBotUuid(){
		this.publicUuid = UUID.randomUUID().toString();
	}

	public int getDrawPenalties() {
		return drawPenalties;
	}

	public void setDrawPenalties(int drawPenalties) {
		this.drawPenalties = drawPenalties;
	}

	public boolean isStopPartyRequested() {
		return stopPartyRequested;
	}

	public void setStopPartyRequested(boolean stopPartyRequested) {
		this.stopPartyRequested = stopPartyRequested;
	}

	public boolean isBotifyPending() {
		return botifyPending;
	}

	public void setBotifyPending(boolean botifyPending) {
		this.botifyPending = botifyPending;
	}

	public int getDrawn() {
		return drawn;
	}

	public void incrementDrawn() {
		this.drawn++;
	}
}
