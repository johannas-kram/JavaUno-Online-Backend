package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.game.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.controller.response.GameState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameStateService {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    public GameState get(String gameUuid, String playerUuid) throws IllegalArgumentException {
        Game game = gameService.getGame(gameUuid);
        Player player = playerService.getPlayer(playerUuid, game);
        return new GameState(game, player);
    }
}
