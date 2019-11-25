package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ActionControllerSelectColorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameController gameController;

    @Autowired
    private GameService gameService;

    private Game game;

    @Before
    public void setup(){
        String uuid = gameController.createGame();
        game = UnoState.getGame(uuid);
        addPlayers();
        gameService.startGame(game.getUuid());
        game.setDesiredColor(null);
        game.setTurnState(TurnState.SELECT_COLOR);
    }

    @Test
    public void shouldSelectColor() throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        String color = "RED";

        MvcResult mvcResult = this.mockMvc.perform(post("/api/action/select-color/{gameUuid}/{playerUuid}/{color}", gameUuid, playerUuid, color))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("success");
        assertThat(game.getDesiredColor()).isEqualTo(color);
    }

    @Test
    public void shouldFailCausedByInvalidColor() throws Exception {
        Exception expectedException = new IllegalArgumentException("There is no such color.");
        shouldFail("silver", expectedException);
    }

    @Test
    public void shouldFailCausedByInvalidTurnState() throws Exception {
        game.setTurnState(TurnState.DRAW_DUTIES);
        Exception expectedException = new IllegalStateException("turn is in wrong state for this action.");
        shouldFail("RED", expectedException);
    }

    @Test
    public void shouldFailCausedByAnotherTurn() throws Exception {
        game.setCurrentPlayerIndex(1);
        Exception expectedException = new IllegalStateException("it's not your turn.");
        shouldFail("RED", expectedException);
    }

    private void shouldFail(String color, Exception expectedException) throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        String expectedMessage = "failure: " + expectedException;

        MvcResult mvcResult = this.mockMvc.perform(post("/api/action/select-color/{gameUuid}/{playerUuid}/{color}", gameUuid, playerUuid, color))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(expectedMessage);
        assertThat(game.getDesiredColor()).isNull();
    }


    private void addPlayers(){
        Player player = new Player("player name", false);
        Player player2 = new Player("player2 name", false);
        game.putHuman(player);
        game.putHuman(player2);
    }

}
