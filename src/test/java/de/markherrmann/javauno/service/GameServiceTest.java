package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
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
public class GameServiceTest {

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
    public void shouldCreateGame(){
        String uuid = TestHelper.createGame(gameService).getUuid();

        assertThat(uuid).isNotNull();
        assertThat(UnoState.containsGame(uuid)).isTrue();
    }

    @Test
    public void shouldStartGame(){
        prepareGame();

        gameService.startGame(game.getUuid());

        assertStartedGameState();
    }

    @Test
    public void shouldFailStartGameCausedByInvalidLifecycle(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            gameService.startGame(game.getUuid());
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
        assertThat(game.getDrawPile()).isEmpty();
    }

    @Test
    public void shouldFailStartGameCausedByNoPlayers(){
        Exception exception = null;

        try {
            gameService.startGame(game.getUuid());
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NOT_ENOUGH_PLAYERS.getValue());
        assertThat(game.getDrawPile()).isEmpty();
    }

    @Test
    public void shouldFailStartGameCausedByInvalidGameUuid(){
        prepareGame();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            gameService.startGame("invalid");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
        assertThat(game.getDrawPile()).isEmpty();
    }

    private void assertStartedGameState(){
        for(Player player : game.getPlayers()){
            assertThat(player.getCards().size()).isEqualTo(7);
        }
        assertThat(game.getDiscardPile().size()).isEqualTo(1);
        assertThat(game.getDrawPile().size()).isEqualTo(86);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.STARTED_GAME);
        assertThat(game.getParty()).isEqualTo(1);
    }

    private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", true);
    }

}
