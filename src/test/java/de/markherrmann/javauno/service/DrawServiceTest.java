package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.controller.response.DrawnCardResponse;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DrawServiceTest {

    @Autowired
    DrawService drawService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.prepareAndStartGame(gameService, playerService);
    }

    @Test
    public void shouldDrawCardInPutOrDrawState(){
        shouldDraw(TurnState.PUT_OR_DRAW, TurnState.PUT_DRAWN);
    }

    @Test
    public void shouldDrawCardInUnoMistakeStateTwoCards(){
        game.getPlayers().get(0).setDrawPenalties(2);
        shouldDraw(TurnState.DRAW_PENALTIES, TurnState.DRAW_PENALTIES);
    }

    @Test
    public void shouldDrawCardInUnoMistakeStateOneCardDrawDuties(){
        game.setDrawDuties(2);
        game.getPlayers().get(0).setDrawPenalties(1);
        shouldDraw(TurnState.DRAW_PENALTIES, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldDrawCardInUnoMistakeStateOneCardNoDrawDuties(){
        game.getPlayers().get(0).setDrawPenalties(1);
        shouldDraw(TurnState.DRAW_PENALTIES, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldDrawCardInDrawDutiesStateOneCard(){
        game.setDrawDuties(1);
        shouldDraw(TurnState.DRAW_DUTIES, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldDrawCardInDrawDutiesStateTwoCards(){
        game.setDrawDuties(2);
        shouldDraw(TurnState.DRAW_DUTIES, TurnState.DRAW_DUTIES);
    }

    @Test
    public void shouldDrawCardInDrawDutiesOrCumulativeState(){
        game.setDrawDuties(2);
        shouldDraw(TurnState.DRAW_DUTIES_OR_CUMULATIVE, TurnState.DRAW_DUTIES);
    }

    @Test
    public void shouldFailCausedByWrongTurnState(){
        Exception expectedException = new IllegalStateException("turn is in wrong state for this action.");
        shouldFail(TurnState.SELECT_COLOR, expectedException);
    }

    @Test
    public void shouldFailCausedByAnotherTurn(){
        game.setCurrentPlayerIndex(1);
        Exception expectedException = new IllegalStateException("it's not your turn.");
        shouldFail(TurnState.PUT_OR_DRAW, expectedException);
    }

    @Test
    public void shouldFailCausedByWrongGameLifecycle(){
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception expectedException = new IllegalStateException("game is in wrong lifecycle.");
        shouldFail(TurnState.PUT_OR_DRAW, expectedException);
    }

    private void shouldDraw(TurnState turnStateIn, TurnState turnStateOut){
        game.setTurnState(turnStateIn);
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Exception exception = null;
        DrawnCardResponse drawnCardResponse = null;

        try {
            drawnCardResponse = drawService.draw(gameUuid, playerUuid);
        } catch (Exception ex){
            exception = ex;
        }

        assertDrawn(drawnCardResponse, exception, turnStateOut);
    }

    private void shouldFail(TurnState turnState, Exception expectedException){
        game.setTurnState(turnState);
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Exception exception = null;
        DrawnCardResponse drawnCardResponse = null;

        try {
            drawnCardResponse = drawService.draw(gameUuid, playerUuid);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotDrawn(drawnCardResponse, exception, turnState, expectedException);
    }

    private void assertDrawn(DrawnCardResponse drawnCardResponse, Exception exception, TurnState expectedTurnState){
        assertThat(exception).isNull();
        assertThat(drawnCardResponse).isNotNull();
        assertThat(game.getDrawPile().size()).isEqualTo(78);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(8);
        assertThat(game.getPlayers().get(0).getCards().get(7)).isEqualTo(drawnCardResponse.getCard());
        if(TurnState.PUT_DRAWN.equals(expectedTurnState) && !drawnCardResponse.isMatch()){
            assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
        } else {
            assertThat(game.getTurnState()).isEqualTo(expectedTurnState);
        }

    }

    private void assertNotDrawn(DrawnCardResponse drawnCardResponse, Exception exception, TurnState expectedTurnState, Exception expectedException){
        assertThat(exception).isNotNull();
        assertThat(drawnCardResponse).isNull();
        assertThat(exception).isInstanceOf(expectedException.getClass());
        assertThat(exception.getMessage()).isEqualTo(expectedException.getMessage());
        assertThat(game.getDrawPile().size()).isEqualTo(79);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(7);
        assertThat(game.getTurnState()).isEqualTo(expectedTurnState);
    }

}
