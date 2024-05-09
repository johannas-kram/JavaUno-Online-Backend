package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.Player;
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

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FinalizeTurnServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    TurnService turnService;

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
    public void shouldFinalizeNormal() throws Exception {
        shouldFinalize(TurnState.PUT_OR_DRAW, 1, 0);
    }

    @Test
    public void shouldFinalizeSkip() throws Exception {
        game.setSkip(true);
        shouldFinalize(TurnState.PUT_OR_DRAW, 2, 0);
    }

    @Test
    public void shouldFinalizeReversed() throws Exception {
        game.toggleReversed();
        shouldFinalize(TurnState.PUT_OR_DRAW, 3, 0);
    }

    @Test
    public void shouldFinalizeSkipReversed() throws Exception {
        game.setSkip(true);
        game.toggleReversed();
        shouldFinalize(TurnState.PUT_OR_DRAW, 2, 0);
    }

    @Test
    public void shouldFinalizeUnoSaidMistake() throws Exception {
        game.getPlayers().get(0).setUnoSaid(true);
        shouldFinalize(TurnState.PUT_OR_DRAW, 1, 2);
    }

    @Test
    public void shouldFinalizeNotUnoSaidMistake() throws Exception {
        Player player = game.getPlayers().get(0);
        Card card = player.getCards().get(0);
        player.getCards().clear();
        player.addCard(card);
        shouldFinalize(TurnState.PUT_OR_DRAW, 1, 2);
    }

    @Test
    public void shouldFinalizeDrawPenalties() throws Exception {
        game.getPlayers().get(1).setDrawPenalties(2);
        shouldFinalize(TurnState.DRAW_PENALTIES, 1, 0);
    }

    @Test
    public void shouldFinalizeDrawDuties() throws Exception {
        game.setDrawDuties(2);
        shouldFinalize(TurnState.DRAW_DUTIES_OR_CUMULATIVE, 1, 0);
    }

    @Test
    public void shouldFailCausedByInvalidLifecycle() {
        shouldFail(GameLifecycle.SET_PLAYERS, TurnState.FINAL_COUNTDOWN, 0, ExceptionMessage.INVALID_STATE_GAME);
    }

    @Test
    public void shouldFailCausedByInvalidTurnState() {
        shouldFail(GameLifecycle.RUNNING, TurnState.PUT_OR_DRAW, 0, ExceptionMessage.INVALID_STATE_TURN);
    }

    @Test
    public void shouldFailCausedByOthersTurn() {
        shouldFail(GameLifecycle.RUNNING, TurnState.FINAL_COUNTDOWN, 1, ExceptionMessage.NOT_YOUR_TURN);
    }

    private void shouldFinalize(TurnState turnState, int index, int drawPenalties) throws Exception {
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(300);
        assertFinalized(turnState, index, drawPenalties);
        assertThat(new File("./data/games/" + game.getUuid())).exists();
    }

    private void shouldFail(GameLifecycle lifecycle, TurnState turnState, int index, ExceptionMessage exceptionMessage){
        game.setGameLifecycle(lifecycle);
        game.setTurnState(turnState);
        Exception exception = null;

        try {
            turnService.next(game.getUuid(), game.getPlayers().get(index).getUuid());
        }
        catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage.getValue());
    }

    private void assertFinalized(TurnState turnState, int index, int drawPenalties){
        GameLifecycle expectedLifecycle = GameLifecycle.RUNNING;
        assertThat(game.getTurnState()).isEqualTo(turnState);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(index);
        assertThat(game.getPlayers().get(0).getDrawPenalties()).isEqualTo(drawPenalties);
        assertThat(game.getGameLifecycle()).isEqualTo(expectedLifecycle);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.NEXT_TURN);
    }
}
