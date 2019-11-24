package de.markherrmann.javauno.controller;

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
    public @ResponseBody String createGame(){
        return gameService.createGame();
    }

    @PostMapping(value = "/start/{gameUuid}")
    public @ResponseBody String startGame(@PathVariable String gameUuid){
        try {
            gameService.startGame(gameUuid);
            return "success";
        } catch (Exception ex){
            return "failure: "+ex;
        }
    }

}
