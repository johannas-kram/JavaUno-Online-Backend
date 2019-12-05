package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.GameCreateResponse;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping(value = "/create")
    public @ResponseBody
    GameCreateResponse createGame(){
        return new GameCreateResponse(gameService.createGame());
    }

    @PostMapping(value = "/start/{gameUuid}")
    public @ResponseBody GeneralResponse startGame(@PathVariable String gameUuid){
        try {
            gameService.startGame(gameUuid);
            return new GeneralResponse(true, "success");
        } catch (Exception ex){
            return new GeneralResponse(false, "failure: " + ex);
        }
    }

}
