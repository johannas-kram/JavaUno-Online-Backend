package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Color;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.push.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectColorService {

    private final TurnService turnService;
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectColorService.class);

    @Autowired
    public SelectColorService(TurnService turnService) {
        this.turnService = turnService;
    }

    public void selectColor(String gameUuid, String playerUuid, String color) throws IllegalStateException, IllegalArgumentException {
        Game game = turnService.getGame(gameUuid);
        synchronized (game) {
            Player player = turnService.getPlayer(playerUuid, game);
            preChecks(game, player);
            String colorName = getColorName(color);
            selectColor(game, colorName);
            turnService.pushAction(PushMessage.SELECTED_COLOR, game);
        }
        turnService.finalizeTurn(game);
    }

    static void selectColor(Game game, String colorName){
        game.setDesiredColor(colorName);
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
        LOGGER.info("Successfully selected color. Game: {}; Color: {}", game.getUuid(), colorName);
    }

    private String getColorName(String color) throws IllegalArgumentException {
        try {
            return Color.valueOf(color.toUpperCase()).name();
        } catch(java.lang.IllegalArgumentException ex){
            LOGGER.error("There is no such color. name: {}", color);
            throw new IllegalArgumentException("There is no such color.");
        }
    }

    private void preChecks(Game game, Player player) throws IllegalStateException {
        if(!turnService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            throw new IllegalStateException("game is in wrong lifecycle.");
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.SELECT_COLOR);
        if(!turnService.isPlayersTurn(game, player)){
            throw new IllegalStateException("it's not your turn.");
        }
    }
}
