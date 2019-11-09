package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
import de.markherrmann.javauno.data.state.components.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.plugin.dom.exception.InvalidStateException;

@Service
public class PlayerService {

    @Autowired
    private GameService gameService;

    public String addPlayer(String gameUuid, String name, boolean bot) throws IllegalArgumentException, InvalidStateException {
        Game game = gameService.getGame(gameUuid);
        if(!gameService.isGameInLivecycle(game, GameLifecycle.ADD_PLAYERS)){
            throw new InvalidStateException("Game is started. Players can not be added anymore.");
        }
        Player player = new Player(name, bot);
        game.getPlayer().put(player.getUuid(), player);
        game.getPlayerList().add(player);
        return game.getUuid();
    }


    Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getPlayer().containsKey(playerUuid)){
            throw new IllegalArgumentException("There is no player with uuid " +playerUuid + " in game with uuid " + game.getUuid());
        }
        return game.getPlayer().get(playerUuid);
    }
}
