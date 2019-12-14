package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.PutCardRequest;
import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.exceptions.CardDoesNotMatchException;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/action")
public class ActionController {

    private final PutService putService;
    private final DrawService drawService;
    private final SelectColorService selectColorService;
    private final SayUnoService sayUnoService;
    private final KeepService keepService;

    @Autowired
    public ActionController(PutService putService, DrawService drawService,
                            SelectColorService selectColorService, SayUnoService sayUnoService, KeepService keepService) {
        this.putService = putService;
        this.drawService = drawService;
        this.selectColorService = selectColorService;
        this.sayUnoService = sayUnoService;
        this.keepService = keepService;
    }

    public ResponseEntity<GeneralResponse> test(){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
    }

    @PostMapping(value = "/put")
    public ResponseEntity<GeneralResponse> putCard(@RequestBody PutCardRequest putCardRequest){
        try {
            putService.put(putCardRequest.getGameUuid(), putCardRequest.getPlayerUuid(), putCardRequest.getCard(), putCardRequest.getCardIndex());
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.ok(response);
        } catch(Exception exception){
            return ErrorResponseUtil.getErrorResponseEntity(exception);
        }
    }

    @PostMapping(value = "/draw/{gameUuid}/{playerUuid}")
    public @ResponseBody DrawnCardResponse drawCard(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            return drawService.draw(gameUuid, playerUuid);
        } catch(Exception ex){
            return new DrawnCardResponse(ex);
        }
    }

    @PostMapping(value = "/select-color/{gameUuid}/{playerUuid}/{color}")
    public @ResponseBody GeneralResponse selectColor(@PathVariable String gameUuid, @PathVariable String playerUuid, @PathVariable String color){
        try {
            selectColorService.selectColor(gameUuid, playerUuid, color);
            return new GeneralResponse(true,"success");
        } catch(Exception ex){
            return new GeneralResponse(false, "failure: " + ex);
        }
    }

    @PostMapping(value = "/say-uno/{gameUuid}/{playerUuid}")
    public @ResponseBody GeneralResponse sayUno(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            sayUnoService.sayUno(gameUuid, playerUuid);
            return new GeneralResponse(true,"success");
        } catch(Exception ex){
            return new GeneralResponse(false, "failure: " + ex);
        }
    }

    @PostMapping(value = "/keep/{gameUuid}/{playerUuid}")
    public @ResponseBody GeneralResponse keep(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            keepService.keep(gameUuid, playerUuid);
            return new GeneralResponse(true,"success");
        } catch(Exception ex){
            return new GeneralResponse(false, "failure: " + ex);
        }
    }
}
