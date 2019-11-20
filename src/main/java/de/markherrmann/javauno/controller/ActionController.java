package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.LayCardRequest;
import de.markherrmann.javauno.service.LayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/action")
public class ActionController {

    private final LayService layService;

    @Autowired
    public ActionController(LayService layService) {
        this.layService = layService;
    }

    @PostMapping(value = "/lay")
    public @ResponseBody String layCard(@RequestBody LayCardRequest layCardRequest){
        try {
            return layService.lay(layCardRequest.getGameUuid(), layCardRequest.getPlayerUuid(), layCardRequest.getCardString(), layCardRequest.getCardIndex());
        } catch(Exception ex){
            return "failure: " + ex;
        }
    }
}
