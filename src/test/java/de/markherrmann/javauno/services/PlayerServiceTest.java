package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlayerServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        String uuid = gameService.createGame();
        game = UnoState.getGames().get(uuid);
    }

    @Test
    public void shouldAddPlayer(){
        int playersBefore = game.getPlayer().size();

        playerService.addPlayer(game.getUuid(), "player name", false);

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(1);
    }

    @Test
    public void shouldRemovePlayer(){
        prepareGame();
        game.setCurrentPlayerIndex(2);
        int playersBefore = game.getPlayer().size();

        playerService.removePlayer(game.getUuid(), game.getPlayerList().get(1).getUuid());
        int playersNow = game.getPlayer().size();

        assertThat(playersBefore).isEqualTo(4);
        assertThat(playersNow).isEqualTo(3);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(1);
    }

    @Test
    public void shouldFailAddPlayerCausedByInvalidLifecycle(){
        int playersBefore = game.getPlayer().size();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.addPlayer(game.getUuid(), "player name", false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldFailAddPlayerCausedByInvalidGameUuid(){
        int playersBefore = game.getPlayer().size();
        Exception exception = null;

        try {
            playerService.addPlayer("invalid uuid", "player name", false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidLifecycle(){
        prepareGame();
        int playersBefore = game.getPlayer().size();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), game.getPlayerList().get(0).getUuid());
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidGameUuid(){
        prepareGame();
        int playersBefore = game.getPlayer().size();
        Exception exception = null;

        try {
            playerService.removePlayer("invalid uuid", game.getPlayerList().get(0).getUuid());
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidPlayerUuid(){
        prepareGame();
        int playersBefore = game.getPlayer().size();
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), "invalid uuid");
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", true);
        playerService.addPlayer(game.getUuid(), "A Name", false);
    }
}
