package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;
import de.johannaherrmann.javauno.exceptions.IllegalStateException;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlayerServiceStopPartyTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @Test
    public void shouldRequestStopParty(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);

        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REQUEST_STOP_PARTY);
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.RUNNING);
    }

    @Test
    public void shouldRevokeRequestStopParty(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        playerService.revokeRequestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REVOKE_REQUEST_STOP_PARTY);
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
    }

    @Test
    public void shouldNotRequestStopPartyMultipleTimes(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        assertThat(game.getStopPartyRequested()).isEqualTo(1);
    }

    @Test
    public void shouldNotRevokeRequestStopPartyMultipleTimes(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(1).getUuid());
        playerService.revokeRequestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        playerService.revokeRequestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());

        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isFalse();
        assertThat(game.getPlayers().get(1).isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
    }

    @Test
    public void shouldStopParty(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(1).getUuid());
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(2).getUuid());

        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(3).getUuid());

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.STOP_PARTY);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.SET_PLAYERS);
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isFalse();
        assertThat(game.getPlayers().get(1).isStopPartyRequested()).isFalse();
        assertThat(game.getPlayers().get(2).isStopPartyRequested()).isFalse();
        assertThat(game.getPlayers().get(3).isStopPartyRequested()).isFalse();
    }

    @Test
    public void shouldFailRequestStopPartyCausedByInvalidGameUuid(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.requestStopParty("invalid", game.getPlayers().get(0).getUuid());
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
    }

    @Test
    public void shouldFailRequestStopPartyCausedByInvalidPlayerUuid(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.requestStopParty(game.getUuid(), "invalid");
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
    }

    @Test
    public void shouldFailRequestStopPartyCausedByInvalidState(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception exception = null;

        try {
            playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isFalse();
        assertThat(game.getStopPartyRequested()).isEqualTo(0);
    }

    @Test
    public void shouldFailRevokeRequestStopPartyCausedByInvalidGameUuid(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        Exception exception = null;

        try {
            playerService.revokeRequestStopParty("invalid", game.getPlayers().get(0).getUuid());
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
    }

    @Test
    public void shouldFailRevokeRequestStopPartyCausedByInvalidPlayerUuid(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        Exception exception = null;

        try {
            playerService.revokeRequestStopParty(game.getUuid(), "invalid");
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
    }

    @Test
    public void shouldFailRevokeRequestStopPartyCausedByInvalidState(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception exception = null;

        try {
            playerService.requestStopParty(game.getUuid(), game.getPlayers().get(0).getUuid());
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
        assertThat(game.getPlayers().get(0).isStopPartyRequested()).isTrue();
        assertThat(game.getStopPartyRequested()).isEqualTo(1);
    }


    private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", false);
        playerService.addPlayer(game.getUuid(), "A Name", false);
    }
}
