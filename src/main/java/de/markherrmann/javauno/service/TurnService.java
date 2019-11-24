package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class TurnService {

    private final GameService gameService;
    private final PlayerService playerService;
    private final HousekeepingService housekeepingService;
    private final Logger logger = LoggerFactory.getLogger(TurnService.class);

    @Autowired
    public TurnService(GameService gameService, PlayerService playerService, HousekeepingService housekeepingService){
        this.gameService = gameService;
        this.playerService = playerService;
        this.housekeepingService = housekeepingService;
    }

    boolean isPlayersTurn(Game game, Player player){
        int currentIndex = game.getCurrentPlayerIndex();
        Player current = game.getPlayers().get(currentIndex);
        boolean playersTurn = current.equals(player);
        if(!playersTurn){
            logger.warn(String.format(
                    "It's not the players turn. Game: %s; playersIndex: %d; currentPlayerIndex: %d",
                    game.getUuid(),
                    game.getPlayers().indexOf(player),
                    game.getCurrentPlayerIndex()));
        }
        return playersTurn;
    }

    boolean isGameInLifecycle(Game game, GameLifecycle gameLifecycle){
        boolean inLifecycle = game.getGameLifecycle().equals(gameLifecycle);
        if(!inLifecycle){
            logger.error("game is in wrong lifecycle. Game: " + game.getUuid());
        }
        return inLifecycle;
    }

    void failIfInvalidTurnState(Game game, TurnState... validTurnStates) throws IllegalStateException {
        for(TurnState state : validTurnStates){
            if(game.getTurnState().equals(state)){
                return;
            }
        }
        logger.error(String.format("turn is in wrong state for this action. Game: %s; validStates: %s; state: %s",
                game.getUuid(),
                Arrays.asList(validTurnStates),
                game.getTurnState()));
        throw new IllegalStateException("turn is in wrong state for this action.");
    }

    Game getGame(String gameUuid) throws IllegalArgumentException {
        return gameService.getGame(gameUuid);
    }

    Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        return playerService.getPlayer(playerUuid, game);
    }

    void updateLastAction(Game game){
        housekeepingService.updateLastAction(game);
    }
}
