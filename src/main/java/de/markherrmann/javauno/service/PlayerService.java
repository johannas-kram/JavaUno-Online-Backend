package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final GameService gameService;
    private final HousekeepingService housekeepingService;
    private final PushService pushService;
    private final FinalizeTurnService finalizeTurnService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerService.class);

    public int botifyPlayerByRequestCountdownMillis = 10000;

    @Autowired
    public PlayerService(GameService gameService, HousekeepingService housekeepingService, PushService pushService,
                         FinalizeTurnService finalizeTurnService) {
        this.gameService = gameService;
        this.housekeepingService = housekeepingService;
        this.pushService = pushService;
        this.finalizeTurnService = finalizeTurnService;
    }

    public String addPlayer(String gameUuid, String name, boolean bot) throws IllegalArgumentException, IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
                LOGGER.error("Game is started. Players can not be added anymore. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            if(game.getPlayers().size() == 10){
                LOGGER.error("Players Limit reached. Can not add any further players. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.PLAYERS_LIMIT_REACHED.getValue());
            }
            Player player = addPlayer(game, name, bot);
            return player.getUuid();
        }
    }

    public void removePlayer(String gameUuid, String playerUuid, boolean bot, boolean inGame) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!isValidRemoveState(game, inGame)){
                LOGGER.error("Game is in wrong state. " +
                        "Players can not be removed with this endpoint in this state. " +
                        "Game: {}; inGame: {}; state: {}", gameUuid, inGame ? "yes" : "no", game.getGameLifecycle().name());
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            remove(game, playerUuid, bot, inGame);
            boolean removedGame = housekeepingService.removeGameIfNoHumans(game);
            if(removedGame){
                pushService.push(PushMessage.END, game);
                LOGGER.info("Removed Player. Game: {}; Player: {}", gameUuid, playerUuid);
                return;
            }
            if(inGame && GameLifecycle.SET_PLAYERS.equals(game.getGameLifecycle())) {
                pushService.push(PushMessage.STOP_PARTY, game);
                LOGGER.info("Stopped Party successfully. Game: {}", game.getUuid());
            }
            pushService.push(PushMessage.REMOVED_PLAYER, game);
            LOGGER.info("Removed Player. Game: {}; Player: {}", gameUuid, playerUuid);
        }
    }

    public void botifyPlayer(String gameUuid, String playerUuid) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            boolean removedGame = botify(game, playerUuid);
            if(removedGame){
                pushService.push(PushMessage.END, game);
            } else {
                pushService.push(PushMessage.BOTIFIED_PLAYER, game);
            }
        }
        LOGGER.info("Botified Player. Game: {}; Player: {}", gameUuid, playerUuid);
    }

    public void requestBotifyPlayer(String gameUuid, String publicUuid) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            Player player = getPlayerByPublicUuid(publicUuid, game);
            if(player.isBotifyPending()){
                return;
            }
            player.setBotifyPending(true);
            game.setPlayerIndexForPush(game.getPlayers().indexOf(player));
            botifyPlayerByRequest(game, player);
            pushService.push(PushMessage.REQUEST_BOTIFY_PLAYER, game);
            LOGGER.info("Successfully requested botification. Game: {}; Player (publicUuid): {}", gameUuid, publicUuid);
        }
    }

    public void cancelBotifyPlayer(String gameUuid, String playerUuid) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            Player player = getPlayer(playerUuid, game);
            if(!player.isBotifyPending()){
                return;
            }
            player.setBotifyPending(false);
            game.setPlayerIndexForPush(game.getPlayers().indexOf(player));
            game.getBotifyPlayerByRequestThread().interrupt();
            game.removeBotifyPlayerByRequestThread();
            pushService.push(PushMessage.CANCEL_BOTIFY_PLAYER, game);
            LOGGER.info("Successfully canceled botification. Game: {}; Player (publicUuid): {}", gameUuid, playerUuid);
        }
    }

    public void requestStopParty(String gameUuid, String playerUuid){
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
                LOGGER.error("Game is not started. Players can not request party stop in this state. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
        }
        boolean stoppedParty = requestStopParty(game, playerUuid);
        if(stoppedParty){
            pushService.push(PushMessage.STOP_PARTY, game);
            LOGGER.info("Stopped Party successfully. Game: {}", game.getUuid());
        } else {
            pushService.push(PushMessage.REQUEST_STOP_PARTY, game);
            LOGGER.info("Requested Stop Party successfully. Game: {}; Player: {}", game.getUuid(), playerUuid);
        }
    }

    public void revokeRequestStopParty(String gameUuid, String playerUuid){
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
                LOGGER.error("Game is not started. Players can not revoke request party stop in this state. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
        }
        revokeRequestStopParty(game, playerUuid);
        pushService.push(PushMessage.REVOKE_REQUEST_STOP_PARTY, game);
        LOGGER.info("Revoked Request to Stop Party successfully. Game: {}; Player: {}", game.getUuid(), playerUuid);
    }

    private Player addPlayer(Game game, String name, boolean bot){
        Player player = new Player(name, bot);
        if(bot){
            game.putBot(player);
        } else {
            game.putHuman(player);
        }
        LOGGER.info("Added Player. Game: {}; Player: {}", game.getUuid(), player.getUuid());
        pushService.push(PushMessage.ADDED_PLAYER, game);
        return player;
    }

    private void remove(Game game, String playerUuid, boolean bot, boolean inGame){
        Player player;
        if(bot){
            player = getBot(playerUuid, game);
        } else {
            player = getPlayer(playerUuid, game);
        }
        if(inGame) {
            removeInGame(game, player);
        }
        int index = game.getPlayers().indexOf(player);
        game.setPlayerIndexForPush(index);
        fixCurrentPlayerIndex(game, player);
        if(bot){
            game.removeBot(player);
        } else {
            game.removeHuman(player);
        }
    }

    private boolean botify(Game game, String playerUuid){
        Player player = getPlayer(playerUuid, game);
        int index = game.getPlayers().indexOf(player);
        player.setBot(true);
        game.setPlayerIndexForPush(index);
        game.getHumans().remove(playerUuid);
        game.getBots().put(player.getPublicUuid(), player);
        boolean removedGame = housekeepingService.removeGameIfNoHumans(game);
        if(removedGame){
            return true;
        }
        if(isPlayersTurn(game, player)){
            finalizeTurnService.handleBotifiedPlayerTurn(game, player);
        }
        return false;
    }

    private void botifyPlayerByRequest(Game game, Player player){
        Runnable runnable = () -> botifyPlayerByRequestThread(game, player);
        Thread thread = new Thread(runnable);
        game.setBotifyPlayerByRequestThread(thread);
        thread.start();
    }

    private void botifyPlayerByRequestThread(Game game, Player player){
        try {
            Thread.sleep(botifyPlayerByRequestCountdownMillis);
            LOGGER.info("botification: time's up. know doing botify. Game: {}; Player: {}", game.getUuid(), player.getUuid());
            player.setBotifyPending(false);
            botifyPlayer(game.getUuid(), player.getUuid());
        } catch(InterruptedException exception){
            LOGGER.info("botification was canceled. doing nothing. Game: {}; Player: {}", game.getUuid(), player.getUuid());
        }
    }

    private boolean requestStopParty(Game game, String playerUuid){
        Player player = getPlayer(playerUuid, game);
        if(player.isStopPartyRequested()){
            return false;
        }
        int index = game.getPlayers().indexOf(player);
        game.setPlayerIndexForPush(index);
        game.incrementAndGetStopPartyRequested();
        player.setStopPartyRequested(true);
        if(game.getHumans().size() == game.getStopPartyRequested()){
            stopParty(game);
            return true;
        }
        return false;
    }

    private void revokeRequestStopParty(Game game, String playerUuid){
        Player player = getPlayer(playerUuid, game);
        if(!player.isStopPartyRequested()){
            return;
        }
        int index = game.getPlayers().indexOf(player);
        game.setPlayerIndexForPush(index);
        game.decrementAndGetStopPartyRequested();
        player.setStopPartyRequested(false);
    }

    private void stopParty(Game game){
        gameService.stopParty(game);
    }

    private void fixCurrentPlayerIndex(Game game, Player player){
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        int deletedPlayerIndex = game.getPlayers().indexOf(player);
        if(deletedPlayerIndex < currentPlayerIndex){
            game.setCurrentPlayerIndex(currentPlayerIndex-1);
        }
        if(deletedPlayerIndex == currentPlayerIndex){
            game.setCurrentPlayerIndex(0);
        }
        if(deletedPlayerIndex == game.getLastWinner()){
            game.setLastWinner(-1);
        }
    }


    private void moveCardsToDrawPile(Game game, Player player){
        List<Card> cards = player.getCards();
        game.getDrawPile().addAll(0, cards);
    }

    private boolean isValidRemoveState(Game game, boolean inGame){
        if(inGame){
            return gameService.isGameInLifecycle(game, GameLifecycle.RUNNING);
        }
        return gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS);
    }

    private void removeInGame(Game game, Player player){
        if(isPlayersTurn(game, player)){
            LOGGER.error("Game is in wrong state. " +
                    "Bots only can be removed in running game, if it is not their turn. " +
                    "Game: {}", game.getUuid());
            throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
        }
        if(game.getPlayers().size() <= 2){
            gameService.stopParty(game);
            return;
        }
        moveCardsToDrawPile(game, player);
    }

    private boolean isPlayersTurn(Game game, Player player){
        int currentIndex = game.getCurrentPlayerIndex();
        Player current = game.getPlayers().get(currentIndex);
        return current.equals(player);
    }

    public Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        return getPlayerStatic(playerUuid, game);
    }

    public static Player getPlayerStatic(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getHumans().containsKey(playerUuid)){
            LOGGER.error("There is no such player in this game. Game: {}; uuid: {}", game.getUuid(), playerUuid);
            throw new IllegalArgumentException(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        }
        return game.getHumans().get(playerUuid);
    }

    public Player getPlayerByPublicUuid(String publicUuid, Game game) throws IllegalArgumentException {
        for(Player player : game.getPlayers()){
            if(player.getPublicUuid().equals(publicUuid)){
                return player;
            }
        }
        LOGGER.error("There is no such player in this game. Game: {}; playerPublicUuid: {}", game.getUuid(), publicUuid);
        throw new IllegalArgumentException(ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    private Player getBot(String publicUUid, Game game) throws IllegalArgumentException {
        if(!game.getBots().containsKey(publicUUid)){
            LOGGER.error("There is no such bot in this game. Game: {}; botPublicUuid: {}", game.getUuid(), publicUUid);
            throw new IllegalArgumentException(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        }
        return game.getBots().get(publicUUid);
    }
}
