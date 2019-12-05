package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.GameStateResponse;
import de.markherrmann.javauno.service.GameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/gameState")
public class GameStateController {

    private final GameStateService gameStateService;

    @Autowired
    public GameStateController(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    @GetMapping(value = "/get/{gameUuid}/{playerUuid}")
    public @ResponseBody
    GameStateResponse getGameState(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            return gameStateService.get(gameUuid, playerUuid);
        } catch(Exception ex){
            return new GameStateResponse(ex);
        }

    }

}
