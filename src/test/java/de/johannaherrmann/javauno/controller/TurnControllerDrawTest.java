package de.johannaherrmann.javauno.controller;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.controller.response.GeneralResponse;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.data.state.component.TurnState;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.service.GameService;
import de.johannaherrmann.javauno.exceptions.IllegalStateException;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;

import de.johannaherrmann.javauno.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class TurnControllerDrawTest {

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
    public void shouldDrawCard() throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();

        this.mockMvc.perform(post("/api/turn/draw/{gameUuid}/{playerUuid}", gameUuid, playerUuid))
                .andExpect(status().isOk());

        assertThat(game.getDrawPile().size()).isEqualTo(92);
    }

    @Test
    public void shouldDrawCards() throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
        game.setDrawDuties(4);

        this.mockMvc.perform(post("/api/turn/draw-multiple/{gameUuid}/{playerUuid}", gameUuid, playerUuid))
                .andExpect(status().isOk());

        assertThat(game.getDrawPile().size()).isEqualTo(89);
    }

    @Test
    public void shouldFailCausedByNoSuchGame() throws Exception {
        UnoState.removeGame(game.getUuid());
        Exception expectedException = new IllegalArgumentException(ExceptionMessage.NO_SUCH_GAME.getValue());
        shouldFail(expectedException, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailCausedByNoSuchPlayer() throws Exception {
        game.getPlayers().clear();
        game.getPlayers().add(new Player("test", false));
        for(int i = 1; i <= 7; i++){
            game.getPlayers().get(0).addCard(game.getTopCard());
        }
        Exception expectedException = new IllegalArgumentException(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        shouldFail(expectedException, HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldFailCausedByInvalidTurnState() throws Exception {
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
        Exception expectedException = new IllegalStateException(ExceptionMessage.INVALID_STATE_TURN.getValue());
        shouldFail(expectedException, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldFailCausedByAnotherTurn() throws Exception {
        game.setCurrentPlayerIndex(1);
        Exception expectedException = new IllegalStateException(ExceptionMessage.NOT_YOUR_TURN.getValue());
        shouldFail(expectedException, HttpStatus.BAD_REQUEST);
    }

    private void shouldFail(Exception expectedException, HttpStatus httpStatus) throws Exception {
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/turn/draw/{gameUuid}/{playerUuid}", gameUuid, playerUuid))
                .andExpect(status().is(httpStatus.value()))
                .andReturn();

        assertNotDrawn(mvcResult, expectedException);
    }

    private void assertNotDrawn(MvcResult mvcResult, Exception expectedException) throws Exception {
        GeneralResponse generalResponse = jsonToGeneralResponse(mvcResult.getResponse().getContentAsString());
        assertThat(generalResponse.isSuccess()).isFalse();
        assertThat(generalResponse.getMessage()).isEqualTo("failure: " + expectedException);
        assertThat(game.getDrawPile().size()).isEqualTo(93);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(7);
    }

    private GeneralResponse jsonToGeneralResponse(final String json){
        try {
            return new ObjectMapper().readValue(json, GeneralResponse.class);
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
