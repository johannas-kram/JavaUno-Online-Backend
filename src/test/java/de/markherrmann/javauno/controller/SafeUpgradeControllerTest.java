package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.service.GameService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class SafeUpgradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    private String gameUuid;

    @Before
    public void setup(){
        gameUuid = gameService.createGame();
    }

    @After
    public void tearDown(){
        UnoState.removeGame(gameUuid);
    }

    @Test
    public void shouldReturnSafe() throws Exception {
        UnoState.removeGame(gameUuid);


        MvcResult mvcResult = this.mockMvc.perform(get("/api/upgrade/safe"))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).isEqualTo("safe");
    }

    @Test
    public void shouldReturnUnsafe() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/upgrade/safe"))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        assertThat(response).isEqualTo("unsafe");
    }

}
