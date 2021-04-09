package de.markherrmann.javauno.service;

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

@Service
public class PlayerService {

    private final GameService gameService;
    private final HousekeepingService housekeepingService;
    private final PushService pushService;
    private final FinalizeTurnService finalizeTurnService;

    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);

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
                logger.error("Game is started. Players can not be added anymore. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            if(game.getPlayers().size() == 10){
                logger.error("Players Limit reached. Can not add any further players. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.PLAYERS_LIMIT_REACHED.getValue());
            }
            Player player = addPlayer(game, name, bot);
            return player.getUuid();
        }
    }

    public void removePlayer(String gameUuid, String playerUuid, boolean bot) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
                logger.error("Game is started. Players can not be removed anymore. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            remove(game, playerUuid, bot);
            boolean removedGame = housekeepingService.removeGameIfNoHumans(game);
            if(removedGame){
                pushService.push(PushMessage.END, game);
            } else {
                pushService.push(PushMessage.REMOVED_PLAYER, game);
            }
        }
        logger.info("Removed Player. Game: {}; Player: {}", gameUuid, playerUuid);
    }

    public void botifyPlayer(String gameUuid, String playerUuid) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
                logger.error("Game is not started. Players can not be botified in this state. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            botify(game, playerUuid);
            boolean removedGame = housekeepingService.removeGameIfNoHumans(game);
            if(removedGame){
                pushService.push(PushMessage.END, game);
            } else {
                pushService.push(PushMessage.BOTIFIED_PLAYER, game);
            }
        }
        logger.info("Botified Player. Game: {}; Player: {}", gameUuid, playerUuid);
    }

    public void requestStopParty(String gameUuid, String playerUuid){
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
                logger.error("Game is not started. Players can not request party stop in this state. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
        }
        boolean stoppedParty = requestStopParty(game, playerUuid);
        if(stoppedParty){
            pushService.push(PushMessage.STOP_PARTY, game);
            logger.info("Stopped Party successfully. Game: {}", game.getUuid());
        } else {
            pushService.push(PushMessage.REQUEST_STOP_PARTY, game);
            logger.info("Requested Stop Party successfully. Game: {}; Player: {}", game.getUuid(), playerUuid);
        }
    }

    public void revokeRequestStopParty(String gameUuid, String playerUuid){
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
                logger.error("Game is not started. Players can not revoke request party stop in this state. Game: {}", gameUuid);
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
        }
        revokeRequestStopParty(game, playerUuid);
        pushService.push(PushMessage.REVOKE_REQUEST_STOP_PARTY, game);
        logger.info("Revoked Request to Stop Party successfully. Game: {}; Player: {}", game.getUuid(), playerUuid);
    }

    private Player addPlayer(Game game, String name, boolean bot){
        Player player = new Player(name, bot);
        if(bot){
            game.putBot(player);
        } else {
            game.putHuman(player);
        }
        logger.info("Added Player. Game: {}; Player: {}", game.getUuid(), player.getUuid());
        pushService.push(PushMessage.ADDED_PLAYER, game);
        return player;
    }

    private void remove(Game game, String playerUuid, boolean bot){
        Player player;
        if(bot){
            player = getBot(playerUuid, game);
        } else {
            player = getPlayer(playerUuid, game);
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

    private void botify(Game game, String playerUuid){
        Player player = getPlayer(playerUuid, game);
        int index = game.getPlayers().indexOf(player);
        player.setBot(true);
        player.setBotUuid();
        game.setPlayerIndexForPush(index);
        game.removeHuman(player);
        game.putBot(player);
        if(isPlayersTurn(game, player)){
            finalizeTurnService.handleBotTurn(game, player);
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

    Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getHumans().containsKey(playerUuid)){
            logger.error("There is no such player in this game. Game: {}; uuid: {}", game.getUuid(), playerUuid);
            throw new IllegalArgumentException(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        }
        return game.getHumans().get(playerUuid);
    }

    private Player getBot(String botUuid, Game game) throws IllegalArgumentException {
        if(!game.getBots().containsKey(botUuid)){
            logger.error("There is no such bot in this game. Game: {}; uuid: {}", game.getUuid(), botUuid);
            throw new IllegalArgumentException(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        }
        return game.getBots().get(botUuid);
    }
}
