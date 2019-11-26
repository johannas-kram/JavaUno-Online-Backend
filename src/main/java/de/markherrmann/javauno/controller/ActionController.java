package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.PutCardRequest;
import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.service.DrawService;
import de.markherrmann.javauno.service.PutService;
import de.markherrmann.javauno.service.SayUnoService;
import de.markherrmann.javauno.service.SelectColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/action")
public class ActionController {

    private final PutService putService;
    private final DrawService drawService;
    private final SelectColorService selectColorService;
    private final SayUnoService sayUnoService;

    @Autowired
    public ActionController(PutService putService, DrawService drawService,
                            SelectColorService selectColorService, SayUnoService sayUnoService) {
        this.putService = putService;
        this.drawService = drawService;
        this.selectColorService = selectColorService;
        this.sayUnoService = sayUnoService;
    }

    @PostMapping(value = "/put")
    public @ResponseBody String putCard(@RequestBody PutCardRequest putCardRequest){
        try {
            return putService.put(putCardRequest.getGameUuid(), putCardRequest.getPlayerUuid(), putCardRequest.getCard(), putCardRequest.getCardIndex());
        } catch(Exception ex){
            return "failure: " + ex;
        }
    }

    @GetMapping(value = "/draw/{gameUuid}/{playerUuid}")
    public @ResponseBody DrawnCardResponse drawCard(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            Card card = drawService.draw(gameUuid, playerUuid);
            return new DrawnCardResponse(card);
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
}
