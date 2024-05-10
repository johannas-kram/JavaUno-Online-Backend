package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
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
public class PlayerServiceBotifyTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @MockBean
    private PersistenceService persistenceService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @Test
    public void shouldBotifyPlayer(){
        prepareGame();
        game.setCurrentPlayerIndex(2);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        int playersBefore = game.getPlayers().size();

        playerService.botifyPlayer(game.getUuid(), game.getPlayers().get(1).getUuid());
        int playersNow = game.getPlayers().size();

        assertThat(playersBefore).isEqualTo(4);
        assertThat(playersNow).isEqualTo(4);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(2);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.BOTIFIED_PLAYER);
        assertThat(game.getPlayers().get(1).isBot()).isTrue();
        assertThat(game.getPlayers().get(1).getPublicUuid()).isNotNull();
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidGameUuid(){
        prepareGame();
        Exception exception = null;
        game.setGameLifecycle(GameLifecycle.RUNNING);

        try {
            playerService.botifyPlayer("invalid uuid", game.getPlayers().get(0).getUuid());
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailBotifyPlayerCausedByInvalidPlayerUuid(){
        prepareGame();
        Exception exception = null;
        game.setGameLifecycle(GameLifecycle.RUNNING);

        try {
            playerService.botifyPlayer(game.getUuid(), "invalid uuid");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", true);
        playerService.addPlayer(game.getUuid(), "A Name", false);
    }
}
