package de.johannaherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.controller.request.SendMessageRequest;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.exceptions.*;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;
import de.johannaherrmann.javauno.exceptions.IllegalStateException;
import de.johannaherrmann.javauno.service.GameService;

import de.johannaherrmann.javauno.service.TokenService;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    public void shouldTellTokenizedGameCreateFeatureDisabled() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/game/tokenized-game-create-enabled"))
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo("off");
    }

    @Test
    public void shouldTellTokenizedGameCreateFeatureEnabled() throws Exception {
        given(tokenService.isFeatureEnabled()).willReturn(true);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/game/tokenized-game-create-enabled"))
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo("on");
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

    @Test
    public void shouldAddMessage() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        Player player = game.getPlayers().get(0);
        String testContent = "test content";

        this.mockMvc.perform(post("/api/game/chat/send-message", game.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(getSendMessageRequestAsJson(game.getUuid(), player.getUuid(), testContent)))
                .andExpect(status().is(HttpStatus.CREATED.value()));

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.CHAT_MESSAGE);
        assertThat(game.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void shouldFailAddMessageCausedByInvalidGameUuid() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        Player player = game.getPlayers().get(0);
        String testContent = "test content";
        String expectedMessage = String.format("failure: %s: %s", IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_GAME.getValue());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/chat/send-message", game.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(getSendMessageRequestAsJson("invalid", player.getUuid(), testContent)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value())).andReturn();

        assertThat(game.getMessages()).isEmpty();
        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailAddMessageCausedByInvalidPlayerUuid() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        String testContent = "test content";
        String expectedMessage = String.format("failure: %s: %s", IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_PLAYER.getValue());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/chat/send-message", game.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(getSendMessageRequestAsJson(game.getUuid(), "", testContent)))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value())).andReturn();

        assertThat(game.getMessages()).isEmpty();
        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailAddMessageCausedByEmptyMessage() throws Exception {
        Game game = TestHelper.createGame(gameService);
        addPlayer(game);
        Player player = game.getPlayers().get(0);
        String expectedMessage = String.format("failure: %s: %s", EmptyArgumentException.class.getCanonicalName(), ExceptionMessage.EMPTY_CHAT_MESSAGE.getValue());

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/chat/send-message", game.getUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(getSendMessageRequestAsJson("invalid", player.getUuid(), "")))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn();

        assertThat(game.getMessages()).isEmpty();
        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    private void addPlayer(Game game){
        Player player = new Player("player name", false);
        game.putHuman(player);
    }

    private String getSendMessageRequestAsJson(String gameUuid, String playerUuid, String content){
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setGameUuid(gameUuid);
        sendMessageRequest.setPlayerUuid(playerUuid);
        sendMessageRequest.setContent(content);
        return asJsonString(sendMessageRequest);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
