package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    @Autowired
    private GameService gameService;

    @Autowired
    private HousekeepingService housekeepingService;

    public String addPlayer(String gameUuid, String name, boolean bot) throws IllegalArgumentException, IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
                throw new IllegalStateException("Game is started. Players can not be added anymore.");
            }
            Player player = new Player(name, bot);
            if(bot){
                game.putBot(player);
            } else {
                game.putHuman(player);
            }
            return player.getUuid();
        }
    }

    public void removePlayer(String gameUuid, String playerUuid, boolean bot) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
                throw new IllegalStateException("Game is started. Players can not be removed anymore.");
            }
            remove(game, playerUuid, bot);
            housekeepingService.removeGameIfNoHumans(game);
        }
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
            throw new IllegalArgumentException("There is no such player in this game.");
        }
        return game.getHumans().get(playerUuid);
    }

    Player getBot(String botUuid, Game game) throws IllegalArgumentException {
        if(!game.getBots().containsKey(botUuid)){
            throw new IllegalArgumentException("There is no such bot in this game.");
        }
        return game.getBots().get(botUuid);
    }
}
