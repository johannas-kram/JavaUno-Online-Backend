package de.markherrmann.javauno.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
import de.markherrmann.javauno.data.state.components.Player;
import de.markherrmann.javauno.data.state.responses.GameState;
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
public class GameStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameController gameController;

    private Game game;
    private Player player;

    @Before
    public void setup(){
        String uuid = gameController.createGame();
        game = UnoState.getGames().get(uuid);
        addPlayer();
    }

    @Test
    public void shouldGetSetPlayersState() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/gameState/get/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertGameState(mvcResult, GameLifecycle.SET_PLAYERS);
    }

    @Test
    public void shouldGetRunningState() throws Exception {
        addPlayer();
        gameController.startGame(game.getUuid());

        MvcResult mvcResult = this.mockMvc.perform(get("/gameState/get/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertGameState(mvcResult, GameLifecycle.RUNNING);
    }

    private void assertGameState(MvcResult mvcResult, GameLifecycle gameLifecycle) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        GameState gameState = jsonToObject(response);

        assertThat(gameState.getGame().getGameLifecycle()).isEqualTo(gameLifecycle);
        assertThat(gameState.getPlayers()).isNotEmpty();
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static GameState jsonToObject(final String json) {
        try {
            return new ObjectMapper().readValue(json, GameState.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addPlayer(){
        player = new Player("player name", false);
        game.getPlayerList().add(player);
        game.getPlayer().put(player.getUuid(), player);
    }
}
