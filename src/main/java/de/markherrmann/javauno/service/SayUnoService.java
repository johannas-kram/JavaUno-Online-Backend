package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.push.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SayUnoService {

    private final TurnService turnService;
    private static final Logger LOGGER = LoggerFactory.getLogger(SayUnoService.class);

    @Autowired
    public SayUnoService(TurnService turnService) {
        this.turnService = turnService;
    }

    public void sayUno(String gameUuid, String playerUuid) throws IllegalStateException, IllegalArgumentException {
        Game game = turnService.getGame(gameUuid);
        synchronized (game) {
            Player player = turnService.getPlayer(playerUuid, game);
            preChecks(game, player);
            sayUno(game, player);
            turnService.pushAction(PushMessage.SAID_UNO, game);
        }
    }

    static void sayUno(Game game, Player player){
        player.setUnoSaid(true);
        LOGGER.info("Successfully said uno. Game: {}; Player: {}", game.getUuid(), player.getUuid());
    }

    private void preChecks(Game game, Player player) throws IllegalStateException {
        if(!turnService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            throw new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.SELECT_COLOR,
                TurnState.FINAL_COUNTDOWN);
        if(!turnService.isPlayersTurn(game, player)){
            throw new IllegalStateException(ExceptionMessage.NOT_YOUR_TURN.getValue());
        }
    }
}
