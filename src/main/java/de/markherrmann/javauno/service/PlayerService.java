package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private final GameService gameService;
    private final HousekeepingService housekeepingService;
    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    public PlayerService(GameService gameService, HousekeepingService housekeepingService) {
        this.gameService = gameService;
        this.housekeepingService = housekeepingService;
    }

    public String addPlayer(String gameUuid, String name, boolean bot) throws IllegalArgumentException, IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
                logger.error("Game is started. Players can not be added anymore. Game: " + gameUuid);
                throw new IllegalStateException("Game is started. Players can not be added anymore.");
            }
            Player player = new Player(name, bot);
            if(bot){
                game.putBot(player);
            } else {
                game.putHuman(player);
            }
            logger.info("Added Player. Game: " + gameUuid + "; Player: " + player.getUuid());
            return player.getUuid();
        }
    }

    public void removePlayer(String gameUuid, String playerUuid, boolean bot) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
                logger.error("Game is started. Players can not be removed anymore. Game: " + gameUuid);
                throw new IllegalStateException("Game is started. Players can not be removed anymore.");
            }
            remove(game, playerUuid, bot);
            housekeepingService.removeGameIfNoHumans(game);
        }
        logger.info("Removed Player. Game: " + gameUuid + "; Player: " + playerUuid);
    }

    private void remove(Game game, String playerUuid, boolean bot){
        Player player;
        if(bot){
            player = getBot(playerUuid, game);
        } else {
            player = getPlayer(playerUuid, game);
        }
        fixCurrentPlayerIndex(game, player);
        if(bot){
            game.removeBot(player);
        } else {
            game.removeHuman(player);
        }
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
    }

    Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getHumans().containsKey(playerUuid)){
            logger.error("There is no such player in this game. Game: " + game.getUuid() + "; uuid: " + playerUuid);
            throw new IllegalArgumentException("There is no such player in this game.");
        }
        return game.getHumans().get(playerUuid);
    }

    Player getBot(String botUuid, Game game) throws IllegalArgumentException {
        if(!game.getBots().containsKey(botUuid)){
            logger.error("There is no such bot in this game. Game: " + game.getUuid() + "; uuid: " + botUuid);
            throw new IllegalArgumentException("There is no such bot in this game.");
        }
        return game.getBots().get(botUuid);
    }
}
