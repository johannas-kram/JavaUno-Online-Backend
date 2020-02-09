package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class TurnService {

    private final FinalizeTurnService finalizeTurnService;
    private final BotService botService;
    private final GameService gameService;
    private final PlayerService playerService;
    private final HousekeepingService housekeepingService;
    private final PushService pushService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TurnService.class);

    @Autowired
    public TurnService(FinalizeTurnService finalizeTurnService, BotService botService, GameService gameService,
                       PlayerService playerService, HousekeepingService housekeepingService, PushService pushService){
        this.finalizeTurnService = finalizeTurnService;
        this.botService = botService;
        this.gameService = gameService;
        this.playerService = playerService;
        this.housekeepingService = housekeepingService;
        this.pushService = pushService;
    }

    public void next(String gameUuid, String playerUuid){
        Game game = getGame(gameUuid);
        synchronized (game){
            Player player = getPlayer(playerUuid, game);
            if(!this.isGameInLifecycle(game, GameLifecycle.RUNNING)){
                throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
            }
            failIfInvalidTurnState(game, playerUuid, this.getClass(), TurnState.FINAL_COUNTDOWN);
            if(!isPlayersTurn(game, player)){
                throw new IllegalStateException(ExceptionMessage.NOT_YOUR_TURN.getValue());
            }
        }
        finalizeTurn(game);
    }

    void finalizeTurn(Game game){
        Runnable runnable = () -> finalize(game);
        Thread thread = new Thread(runnable);
        thread.start();
    }

    boolean isPlayersTurn(Game game, Player player){
        int currentIndex = game.getCurrentPlayerIndex();
        Player current = game.getPlayers().get(currentIndex);
        boolean playersTurn = current.equals(player);
        if(!playersTurn){
            LOGGER.warn(
                    "It's not the players turn. Game: {}; playersIndex: {}; currentPlayerIndex: {}",
                    game.getUuid(),
                    game.getPlayers().indexOf(player),
                    game.getCurrentPlayerIndex());
        }
        return playersTurn;
    }

    boolean isGameInLifecycle(Game game, GameLifecycle gameLifecycle){
        boolean inLifecycle = game.getGameLifecycle().equals(gameLifecycle);
        if(!inLifecycle){
            LOGGER.error("game is in wrong lifecycle. Game: {}", game.getUuid());
        }
        return inLifecycle;
    }

    void failIfInvalidTurnState(Game game, String playerUuid, Class<?> service, TurnState... validTurnStates) throws IllegalStateException {
        for(TurnState state : validTurnStates){
            if(game.getTurnState().equals(state)){
                return;
            }
        }
        LOGGER.error("turn is in wrong state for this action. Game: {}; Player: {}; validStates: {}; state: {}; Service: {}",
                game.getUuid(),
                playerUuid,
                Arrays.asList(validTurnStates),
                game.getTurnState(),
                service.getSimpleName());
        throw new IllegalStateException(ExceptionMessage.INVALID_STATE_TURN.getValue());
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

    void pushAction(PushMessage message, Game game){
        pushService.push(message, game);
    }

    private void finalize(Game game){
        synchronized (game){
            finalizeTurnService.finalizeTurn(game);
        }
        pushService.push(PushMessage.NEXT_TURN, game);
        Player player = game.getPlayers().get(game.getCurrentPlayerIndex());
        LOGGER.info("Successfully terminated turn. Game: {}; CurrentPlayerIndex: {}", game.getUuid(), game.getCurrentPlayerIndex());
        if(player.isBot()){
            botService.makeTurn(game, player);
            if(player.getCardCount() > 0){
                finalize(game);
            }
        }
    }
}
