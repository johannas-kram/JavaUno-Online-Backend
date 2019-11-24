package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.AddPlayerRequest;
import de.markherrmann.javauno.controller.response.SetPlayerResponse;
import de.markherrmann.javauno.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/player")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

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
        return removePlayer(gameUuid, playerUuid, false);
    }

    @DeleteMapping(value="/removeBot/{gameUuid}/{playerUuid}")
    public @ResponseBody SetPlayerResponse removeBot(@PathVariable String gameUuid, @PathVariable String playerUuid){
        return removePlayer(gameUuid, playerUuid, true);
    }

    private SetPlayerResponse removePlayer(String gameUuid, String playerUuid, boolean bot){
        try {
            playerService.removePlayer(gameUuid, playerUuid, bot);
            return new SetPlayerResponse(true, "success", playerUuid);
        } catch (Exception ex){
            return new SetPlayerResponse(false, "failure: " + ex, playerUuid);
        }
    }
}
