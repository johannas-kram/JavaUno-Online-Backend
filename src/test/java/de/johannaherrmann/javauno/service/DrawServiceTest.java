package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.TurnState;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.junit.After;
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

    @After
    public void teardown(){
        TestHelper.deleteGames();
    }

    @Test
    public void shouldDrawCardInPutOrDrawStateMatchingCard(){
        game.getDiscardPile().push(game.getDrawPile().peek());
        shouldDraw(TurnState.PUT_OR_DRAW, TurnState.PUT_DRAWN);
    }

    @Test
    public void shouldDrawCardInPutOrDrawStateNotMatchingCard(){
        while(PutService.isMatch(game, game.getDrawPile().peek())){
            Card card = game.getDrawPile().pop();
            game.getDrawPile().add(0, card);
        }
        shouldDraw(TurnState.PUT_OR_DRAW, TurnState.FINAL_COUNTDOWN);
    }

    @Test
    public void shouldDrawCardsInDrawPenaltyState(){
        shouldDrawMultiple(TurnState.DRAW_PENALTIES, TurnState.PUT_OR_DRAW, 2);
    }

    @Test
    public void shouldDrawCardsInDrawPenaltyStateThenDrawDuties(){
        game.setDrawDuties(2);
        shouldDrawMultiple(TurnState.DRAW_PENALTIES, TurnState.DRAW_DUTIES_OR_CUMULATIVE, 2);
    }

    @Test
    public void shouldDrawCardsInDrawDutiesStateTwoCards(){
        game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
        shouldDrawMultiple(TurnState.DRAW_DUTIES_OR_CUMULATIVE, TurnState.PUT_OR_DRAW, 2);
    }

    @Test
    public void shouldDrawCardsInDrawDutiesStateEightCards(){
        game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
        shouldDrawMultiple(TurnState.DRAW_DUTIES_OR_CUMULATIVE, TurnState.PUT_OR_DRAW, 8);
    }

    @Test
    public void shouldFailCausedByWrongTurnState(){
        Exception expectedException = new IllegalStateException(ExceptionMessage.INVALID_STATE_TURN.getValue());
        shouldFail(TurnState.SELECT_COLOR, expectedException);
    }

    @Test
    public void shouldFailCausedByAnotherTurn(){
        game.setCurrentPlayerIndex(1);
        Exception expectedException = new IllegalStateException(ExceptionMessage.NOT_YOUR_TURN.getValue());
        shouldFail(TurnState.PUT_OR_DRAW, expectedException);
    }

    @Test
    public void shouldFailCausedByWrongGameLifecycle(){
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception expectedException = new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
        shouldFail(TurnState.PUT_OR_DRAW, expectedException);
    }

    private void shouldDraw(TurnState turnStateIn, TurnState turnStateOut){
        game.setTurnState(turnStateIn);
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Exception exception = null;

        try {
            drawService.draw(gameUuid, playerUuid);
        } catch (Exception ex){
            exception = ex;
        }

        assertDrawn(exception, turnStateOut);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.DRAWN_CARD);
    }

    private void shouldDrawMultiple(TurnState turnStateIn, TurnState turnStateOut, int count){
        game.setTurnState(turnStateIn);
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Exception exception = null;
        String reason = "duties";
        if(turnStateIn.equals(TurnState.DRAW_PENALTIES)){
            game.getPlayers().get(0).setDrawPenalties(2);
            reason = "penalties";
        } else {
            game.setDrawDuties(count);
        }

        try {
            drawService.drawMultiple(gameUuid, playerUuid);
        } catch (Exception ex){
            exception = ex;
        }

        assertDrawnMultiple(exception, turnStateOut, count, reason);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.DRAWN_CARDS);
    }

    private void shouldFail(TurnState turnState, Exception expectedException){
        game.setTurnState(turnState);
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Exception exception = null;

        try {
            drawService.draw(gameUuid, playerUuid);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotDrawn(exception, turnState, expectedException);
    }

    private void assertDrawn(Exception exception, TurnState expectedTurnState){
        assertThat(exception).isNull();
        assertThat(game.getDrawPile().size()).isEqualTo(78);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(8);
        assertThat(game.getTurnState()).isEqualTo(expectedTurnState);
    }

    private void assertDrawnMultiple(Exception exception, TurnState expectedTurnState, int count, String reason){
        assertThat(exception).isNull();
        assertThat(game.getDrawPile().size()).isEqualTo(79-count);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(7+count);
        assertThat(game.getDrawnCards()).isEqualTo(count);
        assertThat(game.getDrawReason()).isEqualTo(reason);
        if(TurnState.PUT_DRAWN.equals(expectedTurnState)){
            assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
        } else {
            assertThat(game.getTurnState()).isEqualTo(expectedTurnState);
        }
    }

    private void assertNotDrawn(Exception exception, TurnState expectedTurnState, Exception expectedException){
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(expectedException.getClass());
        assertThat(exception.getMessage()).isEqualTo(expectedException.getMessage());
        assertThat(game.getDrawPile().size()).isEqualTo(79);
        assertThat(game.getPlayers().get(0).getCardCount()).isEqualTo(7);
        assertThat(game.getTurnState()).isEqualTo(expectedTurnState);
    }

}
