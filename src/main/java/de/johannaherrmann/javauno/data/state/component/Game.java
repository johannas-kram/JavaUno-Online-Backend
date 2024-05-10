package de.johannaherrmann.javauno.data.state.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.johannaherrmann.javauno.data.fixed.Card;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String uuid;
	private final Map<String, Player> humans = new HashMap<>();
	private final Map<String, Player> bots = new HashMap<>();
	private final List<Player> players = new ArrayList<>();
	private final Stack<Card> discardPile = new Stack<>();
	private final Stack<Card> drawPile = new Stack<>();
	private boolean reversed;
	private String desiredColor;
	private int currentPlayerIndex;
	private GameLifecycle gameLifecycle = GameLifecycle.SET_PLAYERS;
	private Card topCard;
	private long lastAction;
    private TurnState turnState;
    private int drawDuties;
    private boolean skip;
	private int playerIndexForPush;
	private int party;
	private int stopPartyRequested;
	private int lastWinner = -1;
	transient private Thread botifyPlayerByRequestThread;
	private final List<Message> messages = new ArrayList<>();
	private int drawnCards = 0;
	private String drawReason;
	private List<String> previousFirstCardReceivers = new ArrayList<>();

	public Game(){
	    this.uuid = UUID.randomUUID().toString();
    }

    @JsonIgnore
	public String getUuid(){
		return uuid;
	}

	@JsonIgnore
	public Map<String, Player> getHumans(){
		return humans;
	}

	public void putHuman(Player player){
	    humans.put(player.getUuid(), player);
	    addPlayer(player);
    }

    public void removeHuman(Player player){
        humans.remove(player.getUuid());
        removePlayer(player);
    }

    @JsonIgnore
    public Map<String, Player> getBots(){
        return bots;
    }

    public void putBot(Player player){
        bots.put(player.getPublicUuid(), player);
        addPlayer(player);
    }

    public void removeBot(Player player){
        bots.remove(player.getPublicUuid());
        removePlayer(player);
    }

	@JsonIgnore
    public List<Player> getPlayers() {
        return players;
    }

	@JsonIgnore
    public Stack<Card> getDiscardPile(){
		return discardPile;
	}

	@JsonIgnore
	public Stack<Card> getDrawPile(){
		return drawPile;
	}

    public boolean isReversed() {
        return reversed;
    }

    public String getDesiredColor(){
		return desiredColor;
	}

    public void toggleReversed(){
	    reversed = !reversed;
    }

    public void setDesiredColor(String desiredColor) {
        this.desiredColor = desiredColor;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

	public void setCurrentPlayerIndex(int currentPlayerIndex) {
		this.currentPlayerIndex = currentPlayerIndex;
	}

	public GameLifecycle getGameLifecycle() {
		return gameLifecycle;
	}

	public void setGameLifecycle(GameLifecycle gameLifecycle) {
		this.gameLifecycle = gameLifecycle;
	}

    public void setLastAction(long lastAction) {
        this.lastAction = lastAction;
    }

    public long getLastAction(){
	    return lastAction;
    }

	public Card getTopCard(){
		if(discardPile.isEmpty()){
			return null;
		}
		return discardPile.peek();
	}

	private void addPlayer(Player player){
	    players.add(player);
    }

    private void removePlayer(Player player){
        players.remove(player);
    }

    public TurnState getTurnState() {
        return turnState;
    }

    public void setTurnState(TurnState turnState) {
        this.turnState = turnState;
    }

	public int getDrawDuties() {
		return drawDuties;
	}

	public void setDrawDuties(int drawDuties) {
		this.drawDuties = drawDuties;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public void setPlayerIndexForPush(int playerIndexForPush) {
		this.playerIndexForPush = playerIndexForPush;
	}

	@JsonIgnore
	public int getPlayerIndexForPush(){
		return playerIndexForPush;
	}

	public void nextParty() {
		party++;
	}

	public int getParty() {
		return party;
	}

	public int getStopPartyRequested() {
		return stopPartyRequested;
	}

	public void incrementStopPartyRequested(){
		stopPartyRequested++;
	}

	public void decrementStopPartyRequested(){
		stopPartyRequested--;
	}

	public void resetStopPartyRequested() {
		this.stopPartyRequested = 0;
	}

	@JsonIgnore
	public int getLastWinner() {
		return lastWinner;
	}

	public void setLastWinner(int lastWinner) {
		this.lastWinner = lastWinner;
	}

	@JsonIgnore
	public Thread getBotifyPlayerByRequestThread() {
		return botifyPlayerByRequestThread;
	}

	public void setBotifyPlayerByRequestThread(Thread botifyPlayerByRequestThread) {
		this.botifyPlayerByRequestThread = botifyPlayerByRequestThread;
	}

	public void removeBotifyPlayerByRequestThread() {
		this.botifyPlayerByRequestThread = null;
	}

	public void addMessage(Message message){
		messages.add(message);
	}

	public List<Message> getMessages() {
		return messages;
	}

	@JsonIgnore
	public int getDrawnCards() {
		return drawnCards;
	}

	public void setDrawnCards(int drawnCards) {
		this.drawnCards = drawnCards;
	}

	@JsonIgnore
	public String getDrawReason() {
		return drawReason;
	}

	public void setDrawReason(String drawReason) {
		this.drawReason = drawReason;
	}

	public void addPreviousFirstCardReceiver(String uuid){
		previousFirstCardReceivers.add(uuid);
	}

	public void removePreviousFirstCardReceiver(String uuid){
		previousFirstCardReceivers.remove(uuid);
	}

	public boolean wasAlreadyFirstCardReceiver(String uuid){
		if(previousFirstCardReceivers.size() == this.players.size()){
			previousFirstCardReceivers.clear();
		}
		return previousFirstCardReceivers.contains(uuid);
	}
}
