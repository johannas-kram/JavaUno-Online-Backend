package de.markherrmann.javauno.controllers;

import de.markherrmann.javauno.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/game")
public class GameController {

    @Autowired
    private GameService gameService;

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
