package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.*;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.GameService;

import de.markherrmann.javauno.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    @MockBean
    private TokenService tokenService;

    @Test
    public void shouldCreateGame() throws Exception {
        this.mockMvc.perform(post("/api/game/create/empty"))
                .andExpect(status().is(HttpStatus.CREATED.value()));

        assertThat(UnoState.getGamesEntrySet()).isNotEmpty();
    }

    @Test
    public void shouldFailCreateGameCausedByInvalidToken() throws Exception {
        String expectedMessage = "failure: " + InvalidTokenException.class.getCanonicalName();
        UnoState.clear();
        doThrow(InvalidTokenException.class).when(tokenService).checkForTokenizedGameCreate(isA(String.class));

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/create/empty"))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()))
                .andReturn();

        assertThat(UnoState.getGamesEntrySet()).isEmpty();
        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailCreateGameCausedByIOException() throws Exception {
        String expectedMessage = "failure: " + FileReadException.class.getCanonicalName();
        UnoState.clear();
        doThrow(FileReadException.class).when(tokenService).checkForTokenizedGameCreate(isA(String.class));

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/create/empty"))
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andReturn();

        assertThat(UnoState.getGamesEntrySet()).isEmpty();
        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldStartGame() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        addPlayer(game);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo("success");
    }

    @Test
    public void shouldFailStartGameCausedByInvalidUuid() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        String expectedMessage = String.format("failure: %s: %s", IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_GAME.getValue());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", "invalid"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailStartGameCausedByInvalidLifecycle() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        String expectedMessage = String.format("failure: %s: %s", IllegalStateException.class.getCanonicalName(), ExceptionMessage.INVALID_STATE_GAME.getValue());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailStartGameCausedByNoPlayers() throws Exception {
        Game game = TestHelper.createGame(gameService);
        String expectedMessage = String.format("failure: %s: %s", IllegalStateException.class.getCanonicalName(), ExceptionMessage.NOT_ENOUGH_PLAYERS.getValue());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    private void addPlayer(Game game){
        Player player = new Player("player name", false);
        game.putHuman(player);
    }

}
