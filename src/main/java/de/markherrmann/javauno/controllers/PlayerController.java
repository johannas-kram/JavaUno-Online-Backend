package de.markherrmann.javauno.controllers;

import de.markherrmann.javauno.controllers.request.AddPlayerRequest;
import de.markherrmann.javauno.controllers.response.SetPlayerResponse;
import de.markherrmann.javauno.services.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/player")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @PostMapping(value="/add")
    public @ResponseBody SetPlayerResponse addPlayer(@RequestBody AddPlayerRequest addPlayerRequest){
        try {
            String playerUuid = playerService.addPlayer(
                    addPlayerRequest.getGameUuid(),
                    addPlayerRequest.getName(),
                    addPlayerRequest.isBot());
            return new SetPlayerResponse(true, "success", playerUuid);
        } catch (Exception ex){
            return new SetPlayerResponse(false, "failure: " + ex, null);
        }
    }

    @DeleteMapping(value="/remove/{gameUuid}/{playerUuid}")
    public @ResponseBody SetPlayerResponse removePlayer(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            playerService.removePlayer(gameUuid, playerUuid);
            return new SetPlayerResponse(true, "success", playerUuid);
        } catch (Exception ex){
            return new SetPlayerResponse(false, "failure: " + ex, playerUuid);
        }
    }
}
