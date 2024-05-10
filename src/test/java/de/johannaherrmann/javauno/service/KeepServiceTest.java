package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.data.state.component.TurnState;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.exceptions.IllegalStateException;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KeepServiceTest {

    @Autowired
    KeepService keepService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @MockBean
    private PersistenceService persistenceService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.prepareAndStartGame(gameService, playerService);
        game.getPlayers().get(0).setUnoSaid(false);
        game.setTurnState(TurnState.PUT_DRAWN);
    }

    @Test
    public void shouldKeep(){
        String gameUuid = game.getUuid();
        Player player = game.getPlayers().get(0);
        String playerUuid = player.getUuid();

        keepService.keep(gameUuid, playerUuid);

        assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.KEPT_CARD);
    }

    @Test
    public void shouldFailCausedByInvalidLifecycle(){
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception expectedException = new IllegalStateException(ExceptionMessage.INVALID_STATE_GAME.getValue());
        shouldFail(TurnState.PUT_DRAWN, expectedException);
    }

    @Test
    public void shouldFailCausedByInvalidTurnState(){
        game.setTurnState(TurnState.PUT_OR_DRAW);
        Exception expectedException = new IllegalStateException(ExceptionMessage.INVALID_STATE_TURN.getValue());
        shouldFail(TurnState.PUT_OR_DRAW, expectedException);
    }

    @Test
    public void shouldFailCausedByAnotherTurn(){
        game.setCurrentPlayerIndex(1);
        Exception expectedException = new IllegalStateException(ExceptionMessage.NOT_YOUR_TURN.getValue());
        shouldFail(TurnState.PUT_DRAWN, expectedException);
    }

    private void shouldFail(TurnState turnState, Exception expectedException){
        game.setTurnState(turnState);
        String gameUuid = game.getUuid();
        Player player = game.getPlayers().get(0);
        String playerUuid = player.getUuid();
        Exception exception = null;

        try {
            keepService.keep(gameUuid, playerUuid);
        }
        catch(Exception ex){
            exception = ex;
        }

        assertFailed(turnState, exception, expectedException);
    }

    private void assertFailed(TurnState turnState, Exception exception, Exception expectedException){
        assertThat(game.getTurnState()).isEqualTo(turnState);
        assertThat(exception).isInstanceOf(expectedException.getClass());
        assertThat(exception.getMessage()).isEqualTo(expectedException.getMessage());
    }
}
