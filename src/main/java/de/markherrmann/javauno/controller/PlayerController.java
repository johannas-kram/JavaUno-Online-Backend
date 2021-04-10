package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.request.AddPlayerRequest;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.controller.response.SetPlayerResponse;
import de.markherrmann.javauno.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<GeneralResponse> addPlayer(@RequestBody AddPlayerRequest addPlayerRequest){
        try {
            String playerUuid = playerService.addPlayer(
                    addPlayerRequest.getGameUuid(),
                    addPlayerRequest.getName(),
                    addPlayerRequest.isBot());
            SetPlayerResponse response = new SetPlayerResponse(true, "success", playerUuid);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @DeleteMapping(value="/remove/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> removePlayer(@PathVariable String gameUuid, @PathVariable String playerUuid){
        return removePlayer(gameUuid, playerUuid, false, false);
    }

    @DeleteMapping(value="/removeBot/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> removeBot(@PathVariable String gameUuid, @PathVariable String playerUuid){
        return removePlayer(gameUuid, playerUuid, true, false);
    }

    @DeleteMapping(value="/removeBotInGame/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> removeBotInGame(@PathVariable String gameUuid, @PathVariable String playerUuid){
        return removePlayer(gameUuid, playerUuid, true, true);
    }

    @PostMapping(value="/botify/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> botifyPlayer(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            playerService.botifyPlayer(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.ok(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value="/request-stop-party/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> requestStopParty(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            playerService.requestStopParty(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.ok(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value="/revoke-request-stop-party/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> revokeRequestStopParty(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            playerService.revokeRequestStopParty(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.ok(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    private ResponseEntity<GeneralResponse> removePlayer(String gameUuid, String playerUuid, boolean bot, boolean inGame){
        try {
            playerService.removePlayer(gameUuid, playerUuid, bot, inGame);
            SetPlayerResponse response = new SetPlayerResponse(true, "success", playerUuid);
            return ResponseEntity.ok(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }
}
