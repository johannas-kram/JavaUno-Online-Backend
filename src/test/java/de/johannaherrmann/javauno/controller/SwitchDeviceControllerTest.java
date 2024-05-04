package de.johannaherrmann.javauno.controller;

import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.service.GameService;
import de.johannaherrmann.javauno.service.PlayerService;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class SwitchDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private PlayerService playerService;

    @Test
    public void shouldSwitchIn() throws Exception {
        String pushUuid = "testPushUuid";
        String gameUuid = "testGameUuid";
        String playerUuid = "testPlayerUuid";

        MvcResult mvcResult = this.mockMvc.perform(post(
                "/api/switch/in/{pushUuid}/{gameUuid}/{playerUuid}/{sayUno}/{readMessages}",
                            pushUuid, gameUuid, playerUuid, "empty", "empty"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_IN);
        assertThat(mvcResult.getResponse().getContentAsString().contains("success")).isTrue();
    }

    @Test
    public void shouldSetSwitchFinished() throws Exception {
        Game game = new Game();
        Player player = new Player("", false);
        String gameUuid = game.getUuid();
        String playerUuid = player.getUuid();
        given(gameService.getGame(gameUuid)).willReturn(game);
        given(playerService.getPlayer(eq(playerUuid), any(Game.class))).willReturn(player);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/switch/finished/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_FINISHED);
        assertThat(mvcResult.getResponse().getContentAsString().contains("success")).isTrue();
    }

}
