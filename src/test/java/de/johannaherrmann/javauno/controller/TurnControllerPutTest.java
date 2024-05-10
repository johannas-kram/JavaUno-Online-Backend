package de.johannaherrmann.javauno.controller;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.controller.request.PutCardRequest;
import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.data.state.component.TurnState;
import de.johannaherrmann.javauno.exceptions.CardDoesNotMatchException;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;
import de.johannaherrmann.javauno.exceptions.IllegalStateException;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class TurnControllerPutTest {

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
        addPlayers();
        gameService.startGame(game.getUuid());
    }

    @Test
    public void shouldPutCard() throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        int putBefore = game.getDiscardPile().size();
        PutCardRequest putCardRequest = buildValidRequest();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/turn/put")
                .contentType("application/json")
                .content(asJsonString(putCardRequest)))
                .andExpect(status().isOk())
                .andReturn();
        int putNow = game.getDiscardPile().size();

        assertThat(putNow - putBefore).isEqualTo(1);
        assertThat(TestHelper.jsonToPutCardResponseObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo("success");
    }

    @Test
    public void shouldFailCausedByNoSuchGame() throws Exception {
        String expectedMessage = buildExpectedMessage(IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_GAME.getValue());
        UnoState.removeGame(game.getUuid());
        shouldFail(game.getTopCard(), 0, TurnState.PUT_OR_DRAW, expectedMessage, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailCausedByNoSuchPlayer() throws Exception {
        String expectedMessage = buildExpectedMessage(IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_PLAYER.getValue());
        game.getPlayers().clear();
        game.getPlayers().add(new Player("test", false));
        shouldFail(game.getTopCard(), 0, TurnState.PUT_OR_DRAW, expectedMessage, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailCausedByInvalidCard() throws Exception {
        String expectedMessage = buildExpectedMessage(IllegalArgumentException.class.getCanonicalName(), ExceptionMessage.NO_SUCH_CARD.getValue());
        Card wrongCard = TestHelper.findWrongCard(game.getTopCard(), game);
        game.getPlayers().get(0).getCards().add(wrongCard);
        shouldFail(game.getTopCard(), 0, TurnState.PUT_OR_DRAW, expectedMessage, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailCausedByInvalidState() throws Exception {
        String expectedMessage = buildExpectedMessage(IllegalStateException.class.getCanonicalName(), ExceptionMessage.INVALID_STATE_TURN.getValue());
        shouldFail(game.getTopCard(), 0, TurnState.FINAL_COUNTDOWN, expectedMessage, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldFailCausedByWrongTurn() throws Exception {
        String expectedMessage = buildExpectedMessage(IllegalStateException.class.getCanonicalName(), ExceptionMessage.NOT_YOUR_TURN.getValue());
        shouldFail(game.getTopCard(), 1, TurnState.PUT_OR_DRAW, expectedMessage, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldFailCausedByNotMatchingCard() throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        String expectedMessage = "failure: " + CardDoesNotMatchException.class.getCanonicalName();
        shouldFail(game.getTopCard(), 0, TurnState.PUT_DRAWN, expectedMessage, HttpStatus.BAD_REQUEST);
    }

    private void shouldFail(Card card, int playerIndex, TurnState turnState, String expectedMessage, HttpStatus httpStatus) throws Exception {
        game.getPlayers().get(0).addCard(game.getTopCard());
        int putBefore = game.getDiscardPile().size();
        PutCardRequest putCardRequest = buildValidRequest();
        putCardRequest.setCard(card);
        game.setTurnState(turnState);
        game.setCurrentPlayerIndex(playerIndex);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/turn/put")
                .contentType("application/json")
                .content(asJsonString(putCardRequest)))
                .andExpect(status().is(httpStatus.value()))
                .andReturn();
        int putNow = game.getDiscardPile().size();

        assertFailure(mvcResult, expectedMessage, putBefore, putNow);
    }

    private void assertFailure(MvcResult mvcResult, String expectedMessage, int putBefore, int putNow) throws Exception {
        assertThat(putNow - putBefore).isEqualTo(0);
        assertThat(TestHelper.jsonToObject(mvcResult.getResponse().getContentAsString()).getMessage()).isEqualTo(expectedMessage);
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
        putCardRequest.setCard(game.getTopCard());
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
