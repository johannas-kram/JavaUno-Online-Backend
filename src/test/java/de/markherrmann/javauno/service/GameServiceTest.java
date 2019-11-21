package de.markherrmann.javauno.service;

import java.lang.IllegalArgumentException;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
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
        String uuid = gameService.createGame();
        game = UnoState.getGame(uuid);
    }

    @Test
    public void shouldCreateGame(){
        String uuid = gameService.createGame();

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
        assertThat(game.getTakeStack()).isEmpty();
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
        assertThat(game.getTakeStack()).isEmpty();
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
        assertThat(game.getTakeStack()).isEmpty();
    }

    private void assertStartedGameState(){
        for(Player player : game.getPlayers()){
            assertThat(player.getCards().size()).isEqualTo(7);
        }
        assertThat(game.getLayStack().size()).isEqualTo(1);
        assertThat(game.getTakeStack().size()).isEqualTo(86);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
    }

    private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", true);
    }

}
