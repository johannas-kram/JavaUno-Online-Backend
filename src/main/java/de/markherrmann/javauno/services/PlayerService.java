package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
import de.markherrmann.javauno.data.state.components.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    @Autowired
    private GameService gameService;

    public String addPlayer(String gameUuid, String name, boolean bot) throws IllegalArgumentException, IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
            throw new IllegalStateException("Game is started. Players can not be added anymore.");
        }
        Player player = new Player(name, bot);
        game.getPlayer().put(player.getUuid(), player);
        game.getPlayerList().add(player);
        return game.getUuid();
    }

    public void removePlayer(String gameUuid, String playerUuid) throws IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        if(!gameService.isGameInLifecycle(game, GameLifecycle.SET_PLAYERS)){
            throw new IllegalStateException("Game is started. Players can not be removed anymore.");
        }
        Player player = getPlayer(playerUuid, game);
        fixCurrentPlayerIndex(game, player);
        game.getPlayer().remove(player.getUuid());
        game.getPlayerList().remove(player);
    }

    private void fixCurrentPlayerIndex(Game game, Player player){
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        int deletedPlayerIndex = game.getPlayerList().indexOf(player);
        if(deletedPlayerIndex < currentPlayerIndex){
            game.setCurrentPlayerIndex(currentPlayerIndex-1);
        }
        if(deletedPlayerIndex == currentPlayerIndex){
            game.setCurrentPlayerIndex(0);
        }
    }

    Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getPlayer().containsKey(playerUuid)){
            throw new IllegalArgumentException("There is no player with uuid " +playerUuid + " in game with uuid " + game.getUuid());
        }
        return game.getPlayer().get(playerUuid);
    }
}
