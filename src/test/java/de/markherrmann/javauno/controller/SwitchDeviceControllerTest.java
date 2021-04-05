package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.service.SwitchDeviceService;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class SwitchDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

   // @Autowired
   // private SwitchDeviceService switchDeviceService;

    @Test
    public void shouldSwitchIn() throws Exception {
        String pushUuid = "testPushUuid";
        String gameUuid = "testGameUuid";
        String playerUuid = "testPlayerUuid";

        MvcResult mvcResult = this.mockMvc.perform(post("/api/switch/switch-in/{pushUuid}/{gameUuid}/{playerUuid}", pushUuid, gameUuid, playerUuid))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_IN);
        assertThat(mvcResult.getResponse().getContentAsString().contains("success"));
    }

    @Test
    public void shouldSetSwitchFinished() throws Exception {
        String gameUuid = "testGameUuid";
        String playerUuid = "testPlayerUuid";

        MvcResult mvcResult = this.mockMvc.perform(post("/api/switch/switch-finished/{gameUuid}/{playerUuid}", gameUuid, playerUuid))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SWITCH_FINISHED);
        assertThat(mvcResult.getResponse().getContentAsString().contains("success"));
    }

}
