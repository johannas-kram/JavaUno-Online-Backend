package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.PutCardRequest;
import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.service.DrawService;
import de.markherrmann.javauno.service.PutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/action")
public class ActionController {

    private final PutService putService;
    private final DrawService drawService;

    @Autowired
    public ActionController(PutService putService, DrawService drawService) {
        this.putService = putService;
        this.drawService = drawService;
    }

    @PostMapping(value = "/put")
    public @ResponseBody String putCard(@RequestBody PutCardRequest putCardRequest){
        try {
            return putService.put(putCardRequest.getGameUuid(), putCardRequest.getPlayerUuid(), putCardRequest.getCardString(), putCardRequest.getCardIndex());
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
}
