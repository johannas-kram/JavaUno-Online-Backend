package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.SendMessageRequest;
import de.markherrmann.javauno.controller.response.GameCreateResponse;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/game")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping(value = "/create/{token}")
    public ResponseEntity<GeneralResponse> createGame(@PathVariable String token){
        try {
            GameCreateResponse response = new GameCreateResponse(gameService.createGame(token));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/create")
    public ResponseEntity<GeneralResponse> createGame(){
        return createGame("empty");
    }

    @GetMapping(value = "/tokenized-game-create-enabled")
    public ResponseEntity<GeneralResponse> isTokenizedGameCreateFeatureEnabled(){
        try {
            boolean enabled = gameService.isTokenizedGameCreateFeatureEnabled();
            GeneralResponse response = new GeneralResponse(true, enabled ? "on" : "off");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/start/{gameUuid}")
    public ResponseEntity<GeneralResponse> startGame(@PathVariable String gameUuid){
        try {
            gameService.startGame(gameUuid);
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.ok(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/chat/send-message")
    public ResponseEntity<GeneralResponse> addMessage(@RequestBody SendMessageRequest sendMessageRequest){
        try {
            gameService.addMessage(sendMessageRequest.getGameUuid(), sendMessageRequest.getPlayerUuid(), sendMessageRequest.getContent());
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

}
