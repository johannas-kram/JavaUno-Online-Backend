package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.controller.request.LayCardRequest;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
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
public class ActionControllerLayTest {

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
    public void shouldLayCard() throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        int laidBefore = game.getLayStack().size();
        LayCardRequest layCardRequest = buildValidRequest();

        MvcResult mvcResult = this.mockMvc.perform(post("/action/lay")
                .contentType("application/json")
                .content(asJsonString(layCardRequest)))
                .andExpect(status().isOk())
                .andReturn();
        int laidNow = game.getLayStack().size();

        assertThat(laidNow-laidBefore).isEqualTo(1);
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("success");
    }

    @Test
    public void shouldFailCausedByInvalidCard() throws Exception {
        String expectedMessage = buildExpectedMessage("java.lang.IllegalArgumentException", "The Player has no such card at this position.");
        shouldFail("invalid", 0, TurnState.LAY_OR_TAKE, expectedMessage);
    }

    @Test
    public void shouldFailCausedByInvalidState() throws Exception {
        String expectedMessage = buildExpectedMessage("java.lang.IllegalStateException", "Turn is in wrong state for this action.");
        shouldFail(game.getTopCard().toString(), 0, TurnState.TAKE_DUTIES, expectedMessage);
    }

    @Test
    public void shouldFailCausedByWrongTurn() throws Exception {
        shouldFail(game.getTopCard().toString(), 1, TurnState.LAY_OR_TAKE, "failure: it's not your turn.");
    }

    @Test
    public void shouldFailCausedByNotMatchingCard() throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        shouldFail(game.getTopCard().toString(), 0, TurnState.LAY_TAKEN, "failure: card does not match.");
    }

    private void shouldFail(String cardString, int playerIndex, TurnState turnState, String expectedMessage) throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        int laidBefore = game.getLayStack().size();
        LayCardRequest layCardRequest = buildValidRequest();
        layCardRequest.setCardString(cardString);
        game.setTurnState(turnState);
        game.setCurrentPlayerIndex(playerIndex);

        MvcResult mvcResult = this.mockMvc.perform(post("/action/lay")
                .contentType("application/json")
                .content(asJsonString(layCardRequest)))
                .andExpect(status().isOk())
                .andReturn();
        int laidNow = game.getLayStack().size();

        assertFailure(mvcResult, expectedMessage, laidBefore, laidNow);
    }

    private void assertFailure(MvcResult mvcResult, String expectedMessage, int laidBefore, int laidNow) throws Exception {
        assertThat(laidNow-laidBefore).isEqualTo(0);
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo(expectedMessage);
    }

    private String buildExpectedMessage(String exception, String message){
        return "failure: " + exception + ": " + message;
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LayCardRequest buildValidRequest(){
        LayCardRequest layCardRequest = new LayCardRequest();
        layCardRequest.setGameUuid(game.getUuid());
        layCardRequest.setPlayerUuid(game.getPlayers().get(0).getUuid());
        layCardRequest.setCardString(game.getTopCard().toString());
        layCardRequest.setCardIndex(7);
        return layCardRequest;
    }

    private void addPlayers(){
        Player player = new Player("player name", false);
        Player player2 = new Player("player2 name", false);
        game.putHuman(player);
        game.putHuman(player2);
    }

}
