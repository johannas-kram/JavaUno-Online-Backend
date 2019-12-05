package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameController gameController;

    @Test
    public void shouldCreateGame() throws Exception {
        this.mockMvc.perform(post("/api/game/create"))
                .andExpect(status().isOk());

        assertThat(UnoState.getGamesEntrySet()).isNotEmpty();
    }

    @Test
    public void shouldStartGame() throws Exception {
        Game game = createGame();
        addPlayer(game);
        addPlayer(game);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo("success");
    }

    @Test
    public void shouldFailStartGameCausedByInvalidUuid() throws Exception {
        Game game = createGame();
        addPlayer(game);
        String expectedMessage = "failure: de.markherrmann.javauno.exceptions.IllegalArgumentException: There is no such game.";

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", "invalid"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailStartGameCausedByInvalidLifecycle() throws Exception {
        Game game = createGame();
        addPlayer(game);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        String expectedMessage = "failure: de.markherrmann.javauno.exceptions.IllegalStateException: Current round is not finished. New round can not be started yet.";

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    public void shouldFailStartGameCausedByNoPlayers() throws Exception {
        Game game = createGame();
        String expectedMessage = "failure: de.markherrmann.javauno.exceptions.IllegalStateException: There are not enough players in the game.";

        MvcResult mvcResult = this.mockMvc.perform(post("/api/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
    }

    private Game createGame(){
        String gameUuid = gameController.createGame().getGameUuid();
        return UnoState.getGame(gameUuid);
    }

    private void addPlayer(Game game){
        Player player = new Player("player name", false);
        game.putHuman(player);
    }

}
