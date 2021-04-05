package de.markherrmann.javauno.service;

import de.markherrmann.javauno.service.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SwitchDeviceService {

    private final PushService pushService;

    private final Logger logger = LoggerFactory.getLogger(SwitchDeviceService.class);

    @Autowired
    public SwitchDeviceService(PushService pushService) {
        this.pushService = pushService;
    }

    public void setSwitchFinished(String gameUuid, String playerUuid){
        logger.info("Pushing to tell switch {}:{} is finished.", gameUuid, playerUuid);
        pushService.pushDirectly(gameUuid, "switch-finished", playerUuid);
    }

    public void switchIn(String pushUuid, String gameUuid, String playerUuid){
        logger.info("Pushing to switch {}:{} to another (qr-generator) device, using pushUuid {}", gameUuid, playerUuid, pushUuid);
        pushService.pushDirectly(pushUuid, "switch-in", gameUuid, playerUuid);
    }

}
