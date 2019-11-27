package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.PutCardRequest;
import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/action")
public class ActionController {

    private final PutService putService;
    private final DrawService drawService;
    private final SelectColorService selectColorService;
    private final SayUnoService sayUnoService;
    private final RemainService remainService;

    @Autowired
    public ActionController(PutService putService, DrawService drawService,
                            SelectColorService selectColorService, SayUnoService sayUnoService, RemainService remainService) {
        this.putService = putService;
        this.drawService = drawService;
        this.selectColorService = selectColorService;
        this.sayUnoService = sayUnoService;
        this.remainService = remainService;
    }

    @PostMapping(value = "/put")
    public @ResponseBody String putCard(@RequestBody PutCardRequest putCardRequest){
        try {
            return putService.put(putCardRequest.getGameUuid(), putCardRequest.getPlayerUuid(), putCardRequest.getCard(), putCardRequest.getCardIndex());
        } catch(Exception ex){
            return "failure: " + ex;
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
    public @ResponseBody String selectColor(@PathVariable String gameUuid, @PathVariable String playerUuid, @PathVariable String color){
        try {
            selectColorService.selectColor(gameUuid, playerUuid, color);
            return "success";
        } catch(Exception ex){
            return "failure: " + ex;
        }
    }

    @PostMapping(value = "/say-uno/{gameUuid}/{playerUuid}")
    public @ResponseBody String sayUno(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            sayUnoService.sayUno(gameUuid, playerUuid);
            return "success";
        } catch(Exception ex){
            return "failure: " + ex;
        }
    }

    @PostMapping(value = "/remain/{gameUuid}/{playerUuid}")
    public @ResponseBody String remain(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            remainService.remain(gameUuid, playerUuid);
            return "success";
        } catch(Exception ex){
            return "failure: " + ex;
        }
    }
}
