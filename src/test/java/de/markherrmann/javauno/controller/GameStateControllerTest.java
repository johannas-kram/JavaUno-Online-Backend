package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.controller.response.GameState;
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
        game = UnoState.getGame(uuid);
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

    @Test
    public void shouldFailGetStateCausedByInvalidGameUuid() throws Exception {
        addPlayer();
        gameController.startGame(game.getUuid());

        MvcResult mvcResult = this.mockMvc.perform(get("/gameState/get/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertFailure(mvcResult, "There is no such game.");
    }

    @Test
    public void shouldFailGetStateCausedByInvalidPlayerUuid() throws Exception {
        addPlayer();
        gameController.startGame(game.getUuid());

        MvcResult mvcResult = this.mockMvc.perform(get("/gameState/get/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().isOk())
                .andReturn();

        assertFailure(mvcResult, "There is no such player in this game.");
    }

    private void assertGameState(MvcResult mvcResult, GameLifecycle gameLifecycle) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        GameState gameState = jsonToObject(response);

        assertThat(gameState.isSuccess()).isTrue();
        assertThat(gameState.getMessage()).isEqualTo("success");
        assertThat(gameState.getGame().getGameLifecycle()).isEqualTo(gameLifecycle);
        assertThat(gameState.getPlayers()).isNotEmpty();

        if(gameLifecycle.equals(GameLifecycle.RUNNING)){
            assertThat(gameState.getOwnCards().size()).isEqualTo(7);
        }
    }

    private void assertFailure(MvcResult mvcResult, String message) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        GameState gameState = jsonToObject(response);
        String expectedMessage = "failure: java.lang.IllegalArgumentException: " + message;
        assertThat(gameState.isSuccess()).isFalse();
        assertThat(gameState.getMessage()).isEqualTo(expectedMessage);
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
        game.putHuman(player);
    }
}
