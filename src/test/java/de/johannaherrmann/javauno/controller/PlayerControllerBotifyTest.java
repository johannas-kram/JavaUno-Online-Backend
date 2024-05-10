package de.johannaherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.controller.response.SetPlayerResponse;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.data.state.component.TurnState;
import de.johannaherrmann.javauno.service.GameService;
import de.johannaherrmann.javauno.service.PersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private PersistenceService persistenceService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @Test
    public void shouldBotifyPlayer() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Player player = addPlayer();
        game.setTurnState(TurnState.FINAL_COUNTDOWN);

        this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk());

        assertThat(game.getBots().size()).isEqualTo(1);
        assertThat(game.getBots().get(player.getPublicUuid())).isNotNull();
        assertThat(game.getBots().get(player.getPublicUuid()).isBot()).isTrue();
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidGameUuid() throws Exception {
        Player player = addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();


        assertThat(game.getPlayers()).isNotEmpty();
        assertFailure(mvcResult, "de.johannaherrmann.javauno.exceptions.IllegalArgumentException", "There is no such game.");
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidPlayerUuid() throws Exception {
        addPlayer();
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/player/botify/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andReturn();


        assertThat(game.getPlayers()).isNotEmpty();
        assertFailure(mvcResult, "de.johannaherrmann.javauno.exceptions.IllegalArgumentException", "There is no such player in this game.");
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
