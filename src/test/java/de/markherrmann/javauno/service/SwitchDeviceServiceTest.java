package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SwitchDeviceServiceTest {

    @Autowired
    SwitchDeviceService switchDeviceService;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerService playerService;

    @Test
    public void shouldSwitchIn(){
        String pushUuid = "testPushUuid";
        String gameUuid = "testGameUuid";
        String playerUuid = "testPlayerUuid";

        switchDeviceService.switchIn(pushUuid, gameUuid, playerUuid, "empty", "empty");

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_IN);
    }

    @Test
    public void shouldSetSwitchFinished(){
        Game game = new Game();
        Player player = new Player("", false);
        String gameUuid = game.getUuid();
        String playerUuid = player.getUuid();
        given(gameService.getGame(gameUuid)).willReturn(game);
        given(playerService.getPlayer(eq(playerUuid), any(Game.class))).willReturn(player);

        switchDeviceService.setSwitchFinished(gameUuid, playerUuid);

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_FINISHED);
    }

}
