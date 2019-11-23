package de.markherrmann.javauno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.controller.request.PutCardRequest;
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
public class ActionControllerPutTest {

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
    public void shouldPutCard() throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        int putBefore = game.getDiscardPile().size();
        PutCardRequest putCardRequest = buildValidRequest();

        MvcResult mvcResult = this.mockMvc.perform(post("/action/put")
                .contentType("application/json")
                .content(asJsonString(putCardRequest)))
                .andExpect(status().isOk())
                .andReturn();
        int putNow = game.getDiscardPile().size();

        assertThat(putNow - putBefore).isEqualTo(1);
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("success");
    }

    @Test
    public void shouldFailCausedByInvalidCard() throws Exception {
        String expectedMessage = buildExpectedMessage("java.lang.IllegalArgumentException", "The Player has no such card at this position.");
        shouldFail("invalid", 0, TurnState.PUT_OR_DRAW, expectedMessage);
    }

    @Test
    public void shouldFailCausedByInvalidState() throws Exception {
        String expectedMessage = buildExpectedMessage("java.lang.IllegalStateException", "Turn is in wrong state for this action.");
        shouldFail(game.getTopCard().toString(), 0, TurnState.DRAW_DUTIES, expectedMessage);
    }

    @Test
    public void shouldFailCausedByWrongTurn() throws Exception {
        shouldFail(game.getTopCard().toString(), 1, TurnState.PUT_OR_DRAW, "failure: it's not your turn.");
    }

    @Test
    public void shouldFailCausedByNotMatchingCard() throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        shouldFail(game.getTopCard().toString(), 0, TurnState.PUT_DRAWN, "failure: card does not match.");
    }

    private void shouldFail(String cardString, int playerIndex, TurnState turnState, String expectedMessage) throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        int putBefore = game.getDiscardPile().size();
        PutCardRequest putCardRequest = buildValidRequest();
        putCardRequest.setCardString(cardString);
        game.setTurnState(turnState);
        game.setCurrentPlayerIndex(playerIndex);

        MvcResult mvcResult = this.mockMvc.perform(post("/action/put")
                .contentType("application/json")
                .content(asJsonString(putCardRequest)))
                .andExpect(status().isOk())
                .andReturn();
        int putNow = game.getDiscardPile().size();

        assertFailure(mvcResult, expectedMessage, putBefore, putNow);
    }

    private void assertFailure(MvcResult mvcResult, String expectedMessage, int putBefore, int putNow) throws Exception {
        assertThat(putNow - putBefore).isEqualTo(0);
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

    private PutCardRequest buildValidRequest(){
        PutCardRequest putCardRequest = new PutCardRequest();
        putCardRequest.setGameUuid(game.getUuid());
        putCardRequest.setPlayerUuid(game.getPlayers().get(0).getUuid());
        putCardRequest.setCardString(game.getTopCard().toString());
        putCardRequest.setCardIndex(7);
        return putCardRequest;
    }

    private void addPlayers(){
        Player player = new Player("player name", false);
        Player player2 = new Player("player2 name", false);
        game.putHuman(player);
        game.putHuman(player2);
    }

}
