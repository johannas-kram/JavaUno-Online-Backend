package de.markherrmann.javauno.controllers;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
import de.markherrmann.javauno.data.state.components.Player;
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
        this.mockMvc.perform(post("/game/create"))
                .andExpect(status().isOk());

        assertThat(UnoState.getGames()).isNotEmpty();
    }

    @Test
    public void shouldStartGame() throws Exception {
        Game game = createGame();
        addPlayer(game);
        addPlayer(game);

        MvcResult mvcResult = this.mockMvc.perform(post("/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("success");
    }

    @Test
    public void shouldFailStartGameCausedByInvalidUuid() throws Exception {
        Game game = createGame();
        addPlayer(game);

        MvcResult mvcResult = this.mockMvc.perform(post("/game/start/{gameUuid}", "invalid"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("failure: java.lang.IllegalArgumentException: There is no such game.");
    }

    @Test
    public void shouldFailStartGameCausedByInvalidLifecycle() throws Exception {
        Game game = createGame();
        addPlayer(game);
        game.setGameLifecycle(GameLifecycle.RUNNING);

        MvcResult mvcResult = this.mockMvc.perform(post("/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("failure: java.lang.IllegalStateException: Current round is not finished. New round can not be started yet.");
    }

    @Test
    public void shouldFailStartGameCausedByNoPlayers() throws Exception {
        Game game = createGame();

        MvcResult mvcResult = this.mockMvc.perform(post("/game/start/{gameUuid}", game.getUuid()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("failure: java.lang.IllegalStateException: There are not enough players in the game.");
    }

    private Game createGame(){
        String gameUuid = gameController.createGame();
        return UnoState.getGames().get(gameUuid);
    }

    private void addPlayer(Game game){
        Player player = new Player("player name", false);
        game.getPlayerList().add(player);
        game.getPlayer().put(player.getUuid(), player);
    }

}
