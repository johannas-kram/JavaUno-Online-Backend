package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.controller.response.SetPlayerResponse;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
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
public class PlayerControllerStopPartyTest {

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
    public void shouldRequestStopParty() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        addPlayer();

        this.mockMvc.perform(post("/api/player/request-stop-party/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk());

        assertThat(player.isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.RUNNING);
    }

    @Test
    public void shouldRevokeRequestStopParty() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        addPlayer();
        game.incrementStopPartyRequested();
        player.setStopPartyRequested(true);

        this.mockMvc.perform(post("/api/player/revoke-request-stop-party/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk());

        assertThat(player.isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
    }

    @Test
    public void shouldStopParty() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player1 = addPlayer();
        Player player2 = addPlayer();
        game.incrementStopPartyRequested();
        player1.setStopPartyRequested(true);

        this.mockMvc.perform(post("/api/player/request-stop-party/{gameUuid}/{playerUuid}", game.getUuid(), player2.getUuid()))
                .andExpect(status().isOk());

        assertThat(player1.isStopPartyRequested()).isFalse();
        assertThat(player2.isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.SET_PLAYERS);
    }

    @Test
    public void shouldFailRequestStopPartyCausedByInvalidGameUuid() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/request-stop-party/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().is((HttpStatus.NOT_FOUND.value()))).andReturn();

        assertThat(player.isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalArgumentException", ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailRequestStopPartyCausedByInvalidPlayerUuid() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/request-stop-party/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().is((HttpStatus.NOT_FOUND.value()))).andReturn();

        assertThat(player.isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalArgumentException", ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    @Test
    public void shouldFailRequestStopPartyCausedByInvalidState() throws Exception {
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Player player = addPlayer();
        addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/request-stop-party/{gameUuid}/{playerUuid}",
                game.getUuid(), player.getUuid()))
                .andExpect(status().is((HttpStatus.BAD_REQUEST.value()))).andReturn();

        assertThat(player.isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalStateException", ExceptionMessage.INVALID_STATE_GAME.getValue());
    }















    @Test
    public void shouldFailRevokeRequestStopPartyCausedByInvalidGameUuid() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        addPlayer();
        game.incrementStopPartyRequested();
        player.setStopPartyRequested(true);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/revoke-request-stop-party/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().is((HttpStatus.NOT_FOUND.value()))).andReturn();

        assertThat(player.isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalArgumentException", ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailRevokeRequestStopPartyCausedByInvalidPlayerUuid() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        addPlayer();
        game.incrementStopPartyRequested();
        player.setStopPartyRequested(true);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/revoke-request-stop-party/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().is((HttpStatus.NOT_FOUND.value()))).andReturn();

        assertThat(player.isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalArgumentException", ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    @Test
    public void shouldFailRevokeRequestStopPartyCausedByInvalidState() throws Exception {
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Player player = addPlayer();
        addPlayer();
        game.incrementStopPartyRequested();
        player.setStopPartyRequested(true);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/revoke-request-stop-party/{gameUuid}/{playerUuid}",
                game.getUuid(), player.getUuid()))
                .andExpect(status().is((HttpStatus.BAD_REQUEST.value()))).andReturn();

        assertThat(player.isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
        assertFailure(mvcResult, "de.markherrmann.javauno.exceptions.IllegalStateException", ExceptionMessage.INVALID_STATE_GAME.getValue());
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
