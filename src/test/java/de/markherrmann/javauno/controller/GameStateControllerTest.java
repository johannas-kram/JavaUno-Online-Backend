package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.controller.response.GameStateResponse;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.service.GameService;
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

    @Autowired
    private GameService gameService;

    private Game game;
    private Player player;

    @Before
    public void setup(){
        String uuid = gameService.createGame();
        game = UnoState.getGame(uuid);
        addPlayer();
    }

    @Test
    public void shouldGetSetPlayersState() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/gameState/get/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertGameState(mvcResult, GameLifecycle.SET_PLAYERS);
    }

    @Test
    public void shouldGetRunningState() throws Exception {
        addPlayer();
        gameController.startGame(game.getUuid());

        MvcResult mvcResult = this.mockMvc.perform(get("/api/gameState/get/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertGameState(mvcResult, GameLifecycle.RUNNING);
    }

    @Test
    public void shouldFailGetStateCausedByInvalidGameUuid() throws Exception {
        addPlayer();
        gameController.startGame(game.getUuid());

        MvcResult mvcResult = this.mockMvc.perform(get("/api/gameState/get/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertFailure(mvcResult, ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailGetStateCausedByInvalidPlayerUuid() throws Exception {
        addPlayer();
        gameController.startGame(game.getUuid());

        MvcResult mvcResult = this.mockMvc.perform(get("/api/gameState/get/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().isOk())
                .andReturn();

        assertFailure(mvcResult, ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    private void assertGameState(MvcResult mvcResult, GameLifecycle gameLifecycle) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        GameStateResponse gameStateResponse = jsonToObject(response);

        assertThat(gameStateResponse.isSuccess()).isTrue();
        assertThat(gameStateResponse.getMessage()).isEqualTo("success");
        assertThat(gameStateResponse.getGame().getGameLifecycle()).isEqualTo(gameLifecycle);
        assertThat(gameStateResponse.getPlayers()).isNotEmpty();

        if(gameLifecycle.equals(GameLifecycle.RUNNING)){
            assertThat(gameStateResponse.getOwnCards().size()).isEqualTo(7);
        }
    }

    private void assertFailure(MvcResult mvcResult, String message) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        GameStateResponse gameStateResponse = jsonToObject(response);
        String expectedMessage = "failure: de.markherrmann.javauno.exceptions.IllegalArgumentException: " + message;
        assertThat(gameStateResponse.isSuccess()).isFalse();
        assertThat(gameStateResponse.getMessage()).isEqualTo(expectedMessage);
    }

    private static GameStateResponse jsonToObject(final String json) {
        try {
            return new ObjectMapper().readValue(json, GameStateResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addPlayer(){
        player = new Player("player name", false);
        game.putHuman(player);
    }
}
