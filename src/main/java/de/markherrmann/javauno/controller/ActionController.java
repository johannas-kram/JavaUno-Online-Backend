package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.PutCardRequest;
import de.markherrmann.javauno.service.PutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/action")
public class ActionController {

    private final PutService putService;

    @Autowired
    public ActionController(PutService putService) {
        this.putService = putService;
    }

    @PostMapping(value = "/put")
    public @ResponseBody String putCard(@RequestBody PutCardRequest putCardRequest){
        try {
            return putService.put(putCardRequest.getGameUuid(), putCardRequest.getPlayerUuid(), putCardRequest.getCardString(), putCardRequest.getCardIndex());
        } catch(Exception ex){
            return "failure: " + ex;
        }
    }
}
