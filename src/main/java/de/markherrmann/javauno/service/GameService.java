package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.*;
import de.markherrmann.javauno.exceptions.EmptyArgumentException;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import de.markherrmann.javauno.helper.UnoRandom;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Stack;

@Service
public class GameService {

    private final FinalizeTurnService finalizeTurnService;
    private final HousekeepingService housekeepingService;
    private final PushService pushService;
    private final TokenService tokenService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    @Autowired
    public GameService(FinalizeTurnService finalizeTurnService, HousekeepingService housekeepingService,
                       PushService pushService, TokenService tokenService) {
        this.finalizeTurnService = finalizeTurnService;
        this.housekeepingService = housekeepingService;
        this.pushService = pushService;
        this.tokenService = tokenService;
    }

    public String createGame(String token){
        housekeepingService.removeOldGames();
        tokenService.checkForTokenizedGameCreate(token);
        Game game = new Game();
        UnoState.putGame(game);
        housekeepingService.updateLastAction(game);
        LOGGER.info("Created new game with uuid {}", game.getUuid());
        return game.getUuid();
    }

    public boolean isTokenizedGameCreateFeatureEnabled(){
        return tokenService.isFeatureEnabled();
    }

    public void startGame(String gameUuid) throws IllegalArgumentException, IllegalStateException {
        Game game = getGame(gameUuid);
        synchronized (game) {
            if (isGameInLifecycle(game, GameLifecycle.RUNNING)) {
                LOGGER.error("Current round is not finished. New round can not be started yet. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            if (game.getPlayers().size() < 2) {
                LOGGER.error("There are not enough players in the game. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.NOT_ENOUGH_PLAYERS.getValue());
            }
            resetGame(game);
            Stack<Card> deck = Deck.getShuffled();
            game.getDiscardPile().push(deck.pop());
            giveCards(game.getPlayers(), deck);
            game.getDrawPile().addAll(deck);
            game.setGameLifecycle(GameLifecycle.RUNNING);
            game.nextParty();
            housekeepingService.updateLastAction(game);
            LOGGER.info("Started new round. Game: {}", game.getUuid());
            pushService.push(PushMessage.STARTED_GAME, game);
        }
        Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        finalizeTurnService.handleBotTurn(game, currentPlayer);
    }

    public static void finishGame(Game game, Player player){
        game.setLastWinner(game.getPlayers().indexOf(player));
    }

    public void addMessage(String gameUuid, String playerUuid, String content){
        if(content == null || content.trim().isEmpty()){
            throw new EmptyArgumentException(ExceptionMessage.EMPTY_CHAT_MESSAGE.getValue());
        }
        content = content.trim();
        Game game = getGame(gameUuid);
        synchronized (game) {
            Player player = PlayerService.getPlayerStatic(playerUuid, game);
            String publicUuid = player.getPublicUuid();
            Message message = new Message(content, publicUuid, System.currentTimeMillis());
            game.addMessage(message);
            pushService.pushDirectly(game.getUuid(), "chat-message", publicUuid, ""+ message.getTime() ,content);
            LOGGER.info("Successfully added message. Game: {}; Player: {}; Message: {}", gameUuid, playerUuid, content);
        }
    }

    void stopParty(Game game){
        game.getHumans().values().forEach(e->e.setStopPartyRequested(false));
        game.resetStopPartyRequested();
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
    }

    private void resetGame(Game game){
        game.setCurrentPlayerIndex(setAndGetCurrentPlayerIndex(game));
        game.getHumans().values().forEach(e->e.setStopPartyRequested(false));
        if(game.getCurrentPlayerIndex() > 0){
            System.out.println("stop");
        }
        game.resetStopPartyRequested();
        for(Player player : game.getPlayers()){
            player.clearCards();
        }
        game.getDrawPile().clear();
        game.getDiscardPile().clear();
        game.setDesiredColor(null);
        game.setSkip(false);
        game.setDrawDuties(0);
        resetPlayers(game);
        game.setTurnState(TurnState.PUT_OR_DRAW);
        if(game.isReversed()){
            game.toggleReversed();
        }
    }

    private int setAndGetCurrentPlayerIndex(Game game){
        int lastWinner = game.getLastWinner();
        if(lastWinner >= 0){
            return lastWinner;
        }
        int players = game.getPlayers().size();
        return UnoRandom.getRandom().nextInt(players);
    }

    private void resetPlayers(Game game){
        for(Player player : game.getPlayers()){
            player.setUnoSaid(false);
            player.setDrawPenalties(0);
        }
    }

    private void giveCards(List<Player> playerList, Stack<Card> deck){
        for(int cards = 0; cards < 7; cards++){
            for(Player player : playerList){
                player.addCard(deck.pop());
            }
        }
    }

    public Game getGame(String gameUuid) throws IllegalArgumentException {
        return UnoState.getGame(gameUuid);
    }

    boolean isGameInLifecycle(Game game, GameLifecycle gameLifecycle){
        return game.getGameLifecycle().equals(gameLifecycle);
    }
}
