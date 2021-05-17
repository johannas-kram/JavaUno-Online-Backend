package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
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
public class PlayerServiceBotifyByRequestTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @After
    public void teardown(){
        Thread thread = game.getBotifyPlayerByRequestThread();
        if(thread != null){
            thread.interrupt();
            game.removeBotifyPlayerByRequestThread();
        }
    }

    @Test
    public void shouldRequestBotifyPlayer(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);

        playerService.requestBotifyPlayer(game.getUuid(), player.getPublicUuid());

        assertThat(player.isBotifyPending()).isTrue();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REQUEST_BOTIFY_PLAYER);
    }

    @Test
    public void shouldBotifyPlayer(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.botifyPlayerByRequestCountdownMillis = 100;

        playerService.requestBotifyPlayer(game.getUuid(), player.getPublicUuid());
        try {
            Thread.sleep(1100);
        } catch(InterruptedException ex){}

        assertThat(player.isBotifyPending()).isFalse();
        assertThat(player.isBot()).isTrue();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.BOTIFIED_PLAYER);
    }

    @Test
    public void shouldCancelBotifyPlayer(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.requestBotifyPlayer(game.getUuid(), player.getPublicUuid());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex){}

        playerService.cancelBotifyPlayer(game.getUuid(), player.getUuid());

        assertThat(player.isBotifyPending()).isFalse();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.CANCEL_BOTIFY_PLAYER);
    }

    @Test
    public void shouldNotBotifyPlayerBecauseCanceled(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        playerService.botifyPlayerByRequestCountdownMillis = 3000;
        playerService.requestBotifyPlayer(game.getUuid(), player.getPublicUuid());
        try {
            Thread.sleep(1000);
        } catch(InterruptedException ex){}

        playerService.cancelBotifyPlayer(game.getUuid(), player.getUuid());
        try {
            Thread.sleep(200);
        } catch(InterruptedException ex){}

        assertThat(player.isBotifyPending()).isFalse();
        assertThat(player.isBot()).isFalse();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.CANCEL_BOTIFY_PLAYER);
    }

    @Test
    public void shouldFailRequestBotifyPlayerCausedByInvalidGameUuid(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.requestBotifyPlayer("invalid", player.getPublicUuid());
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(player.isBotifyPending()).isFalse();
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailRequestBotifyPlayerCausedByInvalidPublicUuid(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.requestBotifyPlayer(game.getUuid(), "invalid");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(player.isBotifyPending()).isFalse();
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailCancelBotifyPlayerCausedByInvalidGameUuid(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;
        playerService.requestBotifyPlayer(game.getUuid(), player.getPublicUuid());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex){}

        try {
            playerService.cancelBotifyPlayer("invalid", player.getUuid());
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(player.isBotifyPending()).isTrue();
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailCancelBotifyPlayerCausedByInvalidPlayerUuid(){
        Player player = prepareGame();
        game.setCurrentPlayerIndex(0);
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;
        playerService.requestBotifyPlayer(game.getUuid(), player.getPublicUuid());
        try {
            Thread.sleep(2000);
        } catch(InterruptedException ex){}

        try {
            playerService.cancelBotifyPlayer(game.getUuid(), "invalid");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(player.isBotifyPending()).isTrue();
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    private Player prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "Sandrao", false);
        return game.getPlayers().get(2);
    }
}
