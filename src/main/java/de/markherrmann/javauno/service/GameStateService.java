package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.controller.response.GameState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameStateService {

    private final GameService gameService;
    private final PlayerService playerService;
    private final TurnService turnService;

    @Autowired
    public GameStateService(GameService gameService, PlayerService playerService, TurnService turnService) {
        this.gameService = gameService;
        this.playerService = playerService;
        this.turnService = turnService;
    }

    public GameState get(String gameUuid, String playerUuid) throws IllegalArgumentException {
        Game game = gameService.getGame(gameUuid);
        Player player = playerService.getPlayer(playerUuid, game);
        boolean playersTurn = turnService.isPlayersTurn(game, player);
        return new GameState(game, player, playersTurn);
    }
}
