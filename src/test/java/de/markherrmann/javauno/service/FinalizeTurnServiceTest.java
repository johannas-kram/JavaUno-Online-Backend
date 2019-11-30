package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

    private void shouldFinalize(TurnState turnState, int index, int drawPenalties) throws Exception {
        turnService.finalizeTurn(game);
        Thread.sleep(3100);
        assertFinalized(turnState, index, drawPenalties);
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
