package de.markherrmann.javauno.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.controllers.request.AddPlayerRequest;
import de.markherrmann.javauno.controllers.response.SetPlayerResponse;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
import de.markherrmann.javauno.data.state.components.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameController gameController;

    private Game game;

    @Before
    public void setup(){
        String uuid = gameController.createGame();
        game = UnoState.getGames().get(uuid);
    }

    @Test
    public void shouldAddHumanPlayer() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/player/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getAddPlayerRequestAsJson("player name", false)))
                .andExpect(status().isOk()).andReturn();
        Player player = game.getPlayerList().get(0);

        assertThat(player).isNotNull();
        assertThat(player.isBot()).isFalse();
        assertAddPlayerResponse(mvcResult);
    }

    @Test
    public void shouldAddBotPlayer() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/player/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getAddPlayerRequestAsJson("player name", true)))
                .andExpect(status().isOk()).andReturn();
        Player player = game.getPlayerList().get(0);

        assertThat(player).isNotNull();
        assertThat(player.isBot()).isTrue();
        assertAddPlayerResponse(mvcResult);
    }

    @Test
    public void shouldFailAddPlayerCausedByInvalidGameUuid() throws Exception {
        AddPlayerRequest invalidRequest = getAddPlayerRequest("player name", false);
        invalidRequest.setGameUuid("invalid");

        MvcResult mvcResult = this.mockMvc.perform(post("/player/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)))
                .andExpect(status().isOk()).andReturn();

        assertFailure(mvcResult, "java.lang.IllegalArgumentException", "There is no such game.");
    }

    @Test
    public void shouldFailAddPlayerCausedByInvalidLifeCycle() throws Exception {
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(post("/player/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getAddPlayerRequestAsJson("player name", false)))
                .andExpect(status().isOk()).andReturn();

        assertFailure(mvcResult, "java.lang.IllegalStateException", "Game is started. Players can not be added anymore.");
    }

    @Test
    public void shouldRemovePlayer() throws Exception {
        Player player = addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(delete("/player/remove/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk()).andReturn();


        assertThat(game.getPlayerList()).isEmpty();
        assertRemovePlayerResponse(mvcResult);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidGameUuid() throws Exception {
        Player player = addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(delete("/player/remove/{gameUuid}/{playerUuid}", "invalid", player.getUuid()))
                .andExpect(status().isOk()).andReturn();


        assertThat(game.getPlayerList()).isNotEmpty();
        assertFailure(mvcResult, "java.lang.IllegalArgumentException", "There is no such game.");
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidPlayerUuid() throws Exception {
        addPlayer();

        MvcResult mvcResult = this.mockMvc.perform(delete("/player/remove/{gameUuid}/{playerUuid}", game.getUuid(), "invalid"))
                .andExpect(status().isOk()).andReturn();


        assertThat(game.getPlayerList()).isNotEmpty();
        assertFailure(mvcResult, "java.lang.IllegalArgumentException", "There is no such player in this game.");
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidLifecycle() throws Exception {
        Player player = addPlayer();
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(delete("/player/remove/{gameUuid}/{playerUuid}", game.getUuid(), player.getUuid()))
                .andExpect(status().isOk()).andReturn();


        assertThat(game.getPlayerList()).isNotEmpty();
        assertFailure(mvcResult, "java.lang.IllegalStateException", "Game is started. Players can not be removed anymore.");
    }

    private void assertRemovePlayerResponse(MvcResult mvcResult) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        SetPlayerResponse setPlayerResponse = jsonToObject(response);
        assertThat(setPlayerResponse.isSuccess()).isTrue();
        assertThat(setPlayerResponse.getMessage()).isEqualTo("success");
    }

    private void assertAddPlayerResponse(MvcResult mvcResult) throws Exception {
        Player player = game.getPlayerList().get(0);
        String response = mvcResult.getResponse().getContentAsString();
        SetPlayerResponse setPlayerResponse = jsonToObject(response);
        assertThat(setPlayerResponse.isSuccess()).isTrue();
        assertThat(setPlayerResponse.getMessage()).isEqualTo("success");
        assertThat(setPlayerResponse.getPlayerUuid()).isEqualTo(player.getUuid());
    }

    private void assertFailure(MvcResult mvcResult, String exception, String message) throws Exception {
        String response = mvcResult.getResponse().getContentAsString();
        SetPlayerResponse setPlayerResponse = jsonToObject(response);
        String expectedMessage = "failure: " + exception + ": " + message;
        assertThat(setPlayerResponse.isSuccess()).isFalse();
        assertThat(setPlayerResponse.getMessage()).isEqualTo(expectedMessage);
    }

    /*private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", true);
        playerService.addPlayer(game.getUuid(), "A Name", false);
    }*/

    private String getAddPlayerRequestAsJson(String name, boolean bot){
        AddPlayerRequest addPlayerRequest = getAddPlayerRequest(name, bot);
        return asJsonString(addPlayerRequest);
    }

    private AddPlayerRequest getAddPlayerRequest(String name, boolean bot){
        AddPlayerRequest addPlayerRequest = new AddPlayerRequest();
        addPlayerRequest.setGameUuid(game.getUuid());
        addPlayerRequest.setName(name);
        addPlayerRequest.setBot(bot);
        return addPlayerRequest;
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        game.getPlayerList().add(player);
        game.getPlayer().put(player.getUuid(), player);
        return player;
    }
}
