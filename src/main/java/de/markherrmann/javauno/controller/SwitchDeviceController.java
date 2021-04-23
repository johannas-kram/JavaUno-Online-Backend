package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.service.SwitchDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/switch")
public class SwitchDeviceController {

    private final SwitchDeviceService switchDeviceService;

    @Autowired
    public SwitchDeviceController(SwitchDeviceService switchDeviceService) {
        this.switchDeviceService = switchDeviceService;
    }

    @PostMapping(value="/finished/{gameUuid}/{playerUuid}")
    public ResponseEntity<GeneralResponse> setSwitchFinished(@PathVariable String gameUuid, @PathVariable String playerUuid){
        try {
            switchDeviceService.setSwitchFinished(gameUuid, playerUuid);
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

    @PostMapping(value="/in/{pushUuid}/{gameUuid}/{playerUuid}/{sayUno}/{readMessages}")
    public ResponseEntity<GeneralResponse> switchIn(@PathVariable String pushUuid,
                                                    @PathVariable String gameUuid, @PathVariable String playerUuid,
                                                    @PathVariable String sayUno, @PathVariable String readMessages){
        try {
            switchDeviceService.switchIn(pushUuid, gameUuid, playerUuid, sayUno, readMessages);
            GeneralResponse response = new GeneralResponse(true, "success");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception exception){
            return ErrorResponseUtil.getExceptionResponseEntity(exception);
        }
    }

}
