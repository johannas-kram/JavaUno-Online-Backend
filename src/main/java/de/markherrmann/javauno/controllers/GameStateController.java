package de.markherrmann.javauno.controllers;

import de.markherrmann.javauno.data.state.responses.GameState;
import de.markherrmann.javauno.services.GameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/gameState")
public class GameStateController {

    @Autowired
    private GameStateService gameStateService;

    @GetMapping(value = "/get/{gameUuid}/{playerUuid}")
    public @ResponseBody GameState getGameState(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            return gameStateService.get(gameUuid, playerUuid);
        } catch(Exception ex){
            return new GameState(ex);
        }

    }

}
