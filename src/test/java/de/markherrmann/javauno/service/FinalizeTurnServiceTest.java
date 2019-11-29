package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
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
public class FinalizeTurnServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    FinalizeTurnService finalizeTurnService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.prepareAndStartGame(gameService, playerService);
    }

    @Test
    public void shouldFinalizeNormal(){
        shouldFinalize(TurnState.PUT_OR_DRAW, 1, 0, false);
    }

    @Test
    public void shouldFinalizeSkip(){
        game.setSkip(true);
        shouldFinalize(TurnState.PUT_OR_DRAW, 2, 0, false);
    }

    @Test
    public void shouldFinalizeReversed(){
        game.toggleReversed();
        shouldFinalize(TurnState.PUT_OR_DRAW, 3, 0, false);
    }

    @Test
    public void shouldFinalizeSkipReversed(){
        game.setSkip(true);
        game.toggleReversed();
        shouldFinalize(TurnState.PUT_OR_DRAW, 2, 0, false);
    }

    @Test
    public void shouldFinalizeUnoSaidMistake(){
        game.getPlayers().get(0).setUnoSaid(true);
        shouldFinalize(TurnState.PUT_OR_DRAW, 1, 2, false);
    }

    @Test
    public void shouldFinalizeNotUnoSaidMistake(){
        Player player = game.getPlayers().get(0);
        Card card = player.getCards().get(0);
        player.getCards().clear();
        player.addCard(card);
        shouldFinalize(TurnState.PUT_OR_DRAW, 1, 2, false);
    }

    @Test
    public void shouldFinalizeDrawPenalties(){
        game.getPlayers().get(1).setDrawPenalties(2);
        shouldFinalize(TurnState.DRAW_PENALTIES, 1, 0, false);
    }

    @Test
    public void shouldFinalizeDrawDuties(){
        game.setDrawDuties(2);
        shouldFinalize(TurnState.DRAW_DUTIES_OR_CUMULATIVE, 1, 0, false);
    }

    private void shouldFinalize(TurnState turnState, int index, int drawPenalties, boolean finished){
        finalizeTurnService.finalizeTurn(game);

        assertFinalized(turnState, index, drawPenalties, finished);
    }

    private void assertFinalized(TurnState turnState, int index, int drawPenalties, boolean finished){
        GameLifecycle expectedLifecycle = finished ? GameLifecycle.SET_PLAYERS : GameLifecycle.RUNNING;
        assertThat(game.getTurnState()).isEqualTo(turnState);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(index);
        assertThat(game.getPlayers().get(0).getDrawPenalties()).isEqualTo(drawPenalties);
        assertThat(game.getGameLifecycle()).isEqualTo(expectedLifecycle);
    }
}
