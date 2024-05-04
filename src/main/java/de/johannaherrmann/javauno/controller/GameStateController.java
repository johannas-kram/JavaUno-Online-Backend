package de.johannaherrmann.javauno.controller;

import de.johannaherrmann.javauno.controller.response.GameStateResponse;
import de.johannaherrmann.javauno.controller.response.GeneralResponse;
import de.johannaherrmann.javauno.service.GameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<GeneralResponse> getGameState(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            GameStateResponse gameStateResponse = gameStateService.get(gameUuid, playerUuid);
            return ResponseEntity.ok(gameStateResponse);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }

    }

    @GetMapping(value = "/get/{gameUuid}")
    public ResponseEntity<GeneralResponse> getGameState(@PathVariable String gameUuid){
        try {
            GameStateResponse gameStateResponse = gameStateService.get(gameUuid);
            return ResponseEntity.ok(gameStateResponse);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }

    }

}
