package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;
import de.markherrmann.javauno.data.state.responses.GameAddPlayersState;
import de.markherrmann.javauno.data.state.responses.GameBetweenRoundsState;
import de.markherrmann.javauno.data.state.responses.GameRunningState;
import de.markherrmann.javauno.data.state.responses.GameState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameStateService {

    @Autowired
    private GameService gameService;

    public GameState get(String gameUuid, String playerUuid) throws IllegalArgumentException {
        Game game = gameService.getGame(gameUuid);
        Player player;
        switch (game.getGameLifecycle()){
            case ADD_PLAYERS:
                return new GameAddPlayersState(game.getPlayerList());
            case RUNNING:
                player = getPlayer(playerUuid, game);
                return new GameRunningState(game, player);
            case BETWEEN_ROUNDS:
                player = getPlayer(playerUuid, game);
                return new GameBetweenRoundsState(game, player);
        }
        return null;
    }

    private Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getPlayer().containsKey(playerUuid)){
            throw new IllegalArgumentException("There is no player with uuid " +playerUuid + " in game with uuid " + game.getUuid());
        }
        return game.getPlayer().get(playerUuid);
    }
}
