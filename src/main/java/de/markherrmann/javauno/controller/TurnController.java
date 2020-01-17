package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.PutCardRequest;
import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/turn")
public class TurnController {

    private final PutService putService;
    private final DrawService drawService;
    private final SelectColorService selectColorService;
    private final SayUnoService sayUnoService;
    private final KeepService keepService;
    private final TurnService turnService;

    @Autowired
    public TurnController(PutService putService, DrawService drawService,
                          SelectColorService selectColorService, SayUnoService sayUnoService,
                          KeepService keepService, TurnService turnService) {
        this.putService = putService;
        this.drawService = drawService;
        this.selectColorService = selectColorService;
        this.sayUnoService = sayUnoService;
        this.keepService = keepService;
        this.turnService = turnService;
    }

    @PostMapping(value = "/put")
    public ResponseEntity<GeneralResponse> putCard(@RequestBody PutCardRequest putCardRequest){
        try {
            putService.put(putCardRequest.getGameUuid(), putCardRequest.getPlayerUuid(), putCardRequest.getCard(), putCardRequest.getCardIndex());
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.ok(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/draw/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> drawCard(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            DrawnCardResponse drawnCardResponse = drawService.draw(gameUuid, playerUuid);
            return ResponseEntity.ok(drawnCardResponse);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/select-color/{gameUuid}/{playerUuid}/{color}")
    public ResponseEntity<GeneralResponse> selectColor(@PathVariable String gameUuid, @PathVariable String playerUuid, @PathVariable String color){
        try {
            selectColorService.selectColor(gameUuid, playerUuid, color);
            GeneralResponse response = new GeneralResponse(true,"success");
            return ResponseEntity.ok(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/say-uno/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> sayUno(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            sayUnoService.sayUno(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true,"success");
            return ResponseEntity.ok(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/keep/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> keep(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            keepService.keep(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true,"success");
            return ResponseEntity.ok(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value = "/next/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> next(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            turnService.next(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true,"success");
            return ResponseEntity.ok(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }
}
