package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.controller.response.SetPlayerResponse;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.GameService;
import de.markherrmann.javauno.service.PlayerService;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.junit.After;
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
public class PlayerControllerBotifyByRequestTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @After
    public void teardown(){
        Thread thread = game.getBotifyPlayerByRequestThread();
        if(thread != null){
            thread.interrupt();
            game.removeBotifyPlayerByRequestThread();
        }
    }

    @Test
    public void shouldRequestBotifyPlayer() throws Exception {
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);

        this.mockMvc.perform(post("/api/player/request-botify/{gameUuid}/{kickUuid}", game.getUuid(), player.getKickUuid()))
                .andExpect(status().isOk());

        assertThat(player.isBotifyPending()).isTrue();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REQUEST_BOTIFY_PLAYER);
    }

    @Test
    public void shouldCancelBotifyPlayer() throws Exception {
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestBotifyPlayer(game.getUuid(), player.getKickUuid());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex){}

        playerService.cancelBotifyPlayer(game.getUuid(), player.getUuid());
        this.mockMvc.perform(post("/api/player/cancel-botify/{gameUuid}/{kickUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk());

        assertThat(player.isBotifyPending()).isFalse();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.CANCEL_BOTIFY_PLAYER);
    }

    @Test
    public void shouldFailRequestBotifyPlayerCausedByInvalidGameUuid() throws Exception {
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/request-botify/{gameUuid}/{kickUuid}", "invalid", player.getKickUuid()))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();

        assertThat(player.isBotifyPending()).isFalse();
        assertFailure(mvcResult, IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailRequestBotifyPlayerCausedByInvalidKickUuid() throws Exception {
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/request-botify/{gameUuid}/{kickUuid}", game.getUuid(), "invalid"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();

        assertThat(player.isBotifyPending()).isFalse();
        assertFailure(mvcResult, IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    @Test
    public void shouldFailCancelBotifyPlayerCausedByInvalidGameUuid() throws Exception {
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestBotifyPlayer(game.getUuid(), player.getKickUuid());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex){}

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/cancel-botify/{gameUuid}/{kickUuid}", "invalid", player.getUuid()))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();

        assertThat(player.isBotifyPending()).isTrue();
        assertFailure(mvcResult, IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailCancelBotifyPlayerCausedByInvalidPlayerUuid() throws Exception {
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestBotifyPlayer(game.getUuid(), player.getKickUuid());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex){}

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/cancel-botify/{gameUuid}/{kickUuid}", game.getUuid(), "invalid"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();

        assertThat(player.isBotifyPending()).isTrue();
        assertFailure(mvcResult, IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    private Player prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "Sandrao", false);
        return game.getPlayers().get(2);
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
}
