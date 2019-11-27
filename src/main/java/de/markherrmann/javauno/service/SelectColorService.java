package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Color;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectColorService {

    private final TurnService turnService;
    private final Logger logger = LoggerFactory.getLogger(SelectColorService.class);

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
        }
        turnService.finalizeTurn(game);
    }

    private void selectColor(Game game, String colorName){
        game.setDesiredColor(colorName);
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
        logger.info("Successfully set color. Game: " + game.getUuid() + "; Color: " + colorName);
    }

    private String getColorName(String color) throws IllegalArgumentException {
        try {
            String colorName = Color.valueOf(color.toUpperCase()).name();
            return colorName;
        } catch(java.lang.IllegalArgumentException ex){
            logger.error("There is no such color. name: " + color);
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
