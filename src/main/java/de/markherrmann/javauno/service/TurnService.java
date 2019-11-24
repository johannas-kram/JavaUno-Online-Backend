package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TurnService {

    private final GameService gameService;
    private final PlayerService playerService;
    private final HousekeepingService housekeepingService;

    @Autowired
    public TurnService(GameService gameService, PlayerService playerService, HousekeepingService housekeepingService){
        this.gameService = gameService;
        this.playerService = playerService;
        this.housekeepingService = housekeepingService;
    }

    boolean isPlayersTurn(Game game, Player player){
        int currentIndex = game.getCurrentPlayerIndex();
        Player current = game.getPlayers().get(currentIndex);
        return current.equals(player);
    }

    boolean isGameInLifecycle(Game game, GameLifecycle gameLifecycle){
        return game.getGameLifecycle().equals(gameLifecycle);
    }

    void failIfInvalidTurnState(Game game, TurnState... validTurnStates) throws IllegalStateException {
        for(TurnState state : validTurnStates){
            if(game.getTurnState().equals(state)){
                return;
            }
        }
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
