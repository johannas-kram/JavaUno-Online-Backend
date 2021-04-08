package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.controller.response.SetPlayerResponse;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.GameService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class PlayerControllerBotifyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @Test
    public void shouldBotifyPlayer() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();

        this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk());

        assertThat(game.getBots().size()).isEqualTo(1);
        assertThat(game.getBots().get(player.getBotUuid())).isNotNull();
        assertThat(game.getBots().get(player.getBotUuid()).isBot()).isTrue();
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidGameUuid() throws Exception {
        Player player = addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();


        assertThat(game.getPlayers()).isNotEmpty();
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalArgumentException", "There is no such game.");
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidPlayerUuid() throws Exception {
        addPlayer();
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();


        assertThat(game.getPlayers()).isNotEmpty();
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalArgumentException", "There is no such player in this game.");
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidLifecycle() throws Exception {
        Player player = addPlayer();
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();


        assertThat(game.getPlayers()).isNotEmpty();
        assertFailure(mvcResult, IllegalStateException.class.getCanonicalName(), ExceptionMessage.INVALID_STATE_GAME.getValue());
    }

    private void assertFailure(MvcResult mvcResult, String exception, String message) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        SetPlayerResponse setPlayerResponse = jsonToObject(response);
        String expectedMessage = "failure: " + exception + ": " + message;
        assertThat(setPlayerResponse.isSuccess()).isFalse();
        assertThat(setPlayerResponse.getMessage()).isEqualTo(expectedMessage);
    }

    private static SetPlayerResponse jsonToObject(final String json) {
        try {
            return new ObjectMapper().readValue(json, SetPlayerResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Player addPlayer(){
        Player player = new Player("player name", false);
        game.putHuman(player);
        return player;
    }
}
