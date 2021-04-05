package de.markherrmann.javauno.service;

import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SwitchDeviceServiceTest {

    @Autowired
    SwitchDeviceService switchDeviceService;

    @Test
    public void shouldSwitchIn(){
        String pushUuid = "testPushUuid";
        String gameUuid = "testGameUuid";
        String playerUuid = "testPlayerUuid";

        switchDeviceService.switchIn(pushUuid, gameUuid, playerUuid);

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_IN);
    }

    @Test
    public void shouldSetSwitchFinished(){
        String gameUuid = "testGameUuid";
        String playerUuid = "testPlayerUuid";

        switchDeviceService.setSwitchFinished(gameUuid, playerUuid);

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_FINISHED);
    }

}
