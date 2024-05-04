package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.controller.response.GameStateResponse;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameStateService {

    private final GameService gameService;
    private final PlayerService playerService;

    @Autowired
    public GameStateService(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }

    public GameStateResponse get(String gameUuid, String playerUuid) throws IllegalArgumentException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game) {
            Player player = playerService.getPlayer(playerUuid, game);
            int playersIndex = getPlayersIndex(game, player);
            return new GameStateResponse(game, player, playersIndex);
        }
    }

    public GameStateResponse get(String gameUuid) throws IllegalArgumentException {
        Game game = gameService.getGame(gameUuid);
        return new GameStateResponse(game);
    }

    private int getPlayersIndex(Game game, Player player){
        return game.getPlayers().indexOf(player);
    }
}
