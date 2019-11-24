package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.service.GameService;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ActionControllerDrawTest {

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
    }

    @Test
    public void shouldDrawCard() throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();

        MvcResult mvcResult = this.mockMvc.perform(get("/api/action/draw/{gameUuid}/{playerUuid}", gameUuid, playerUuid))
                .andExpect(status().isOk())
                .andReturn();

        assertDrawn(mvcResult);
    }

    @Test
    public void shouldFailCausedByInvalidTurnState() throws Exception {
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
        Exception expectedException = new IllegalStateException("turn is in wrong state for this action.");
        shouldFail(expectedException);
    }

    @Test
    public void shouldFailCausedByAnotherTurn() throws Exception {
        game.setCurrentPlayerIndex(1);
        Exception expectedException = new IllegalStateException("it's not your turn.");
        shouldFail(expectedException);
    }

    private void shouldFail(Exception expectedException) throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();

        MvcResult mvcResult = this.mockMvc.perform(get("/api/action/draw/{gameUuid}/{playerUuid}", gameUuid, playerUuid))
                .andExpect(status().isOk())
                .andReturn();

        assertNotDrawn(mvcResult, expectedException);
    }

    private void assertDrawn(MvcResult mvcResult) throws Exception {
        DrawnCardResponse drawnCardResponse = jsonToObject(mvcResult.getResponse().getContentAsString());
        assertThat(drawnCardResponse.isSuccess()).isTrue();
        assertThat(drawnCardResponse.getMessage()).isEqualTo("success");
        assertThat(drawnCardResponse.getCard()).isNotNull();
        assertThat(game.getDrawPile().size()).isEqualTo(92);
        assertThat(drawnCardResponse.getCard()).isEqualTo(game.getPlayers().get(0).getCards().get(7));
    }

    private void assertNotDrawn(MvcResult mvcResult, Exception expectedException) throws Exception {
        DrawnCardResponse drawnCardResponse = jsonToObject(mvcResult.getResponse().getContentAsString());
        assertThat(drawnCardResponse.isSuccess()).isFalse();
        assertThat(drawnCardResponse.getMessage()).isEqualTo("failure: " + expectedException);
        assertThat(drawnCardResponse.getCard()).isNull();
        assertThat(game.getDrawPile().size()).isEqualTo(93);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(7);
    }

    private DrawnCardResponse jsonToObject(final String json){
        try {
            return new ObjectMapper().readValue(json, DrawnCardResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addPlayers(){
        Player player = new Player("player name", false);
        Player player2 = new Player("player2 name", false);
        game.putHuman(player);
        game.putHuman(player2);
    }
}
