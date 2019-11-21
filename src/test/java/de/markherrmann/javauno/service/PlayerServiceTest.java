package de.markherrmann.javauno.service;

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
public class PlayerServiceTest {

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
    public void shouldAddPlayer(){
        int playersBefore = game.getPlayers().size();

        playerService.addPlayer(game.getUuid(), "player name", false);

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(1);
    }

    @Test
    public void shouldRemovePlayer(){
        prepareGame();
        game.setCurrentPlayerIndex(2);
        int playersBefore = game.getPlayers().size();

        playerService.removePlayer(game.getUuid(), game.getPlayers().get(1).getUuid(), false);
        int playersNow = game.getPlayers().size();

        assertThat(playersBefore).isEqualTo(4);
        assertThat(playersNow).isEqualTo(3);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveBot(){
        prepareGame();
        Player bot = addBot();

        int playersBefore = game.getPlayers().size();

        playerService.removePlayer(game.getUuid(), bot.getBotUuid(), true);
        int playersNow = game.getPlayers().size();

        assertThat(playersBefore).isEqualTo(5);
        assertThat(playersNow).isEqualTo(4);
    }

    @Test
    public void shouldRemoveGameCausedByNoHumanPlayers(){
        prepareGameWithoutBot();
        addBot();

        for(int i = 0; i <= 3; i++){
            playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false);
        }

        assertThat(game.getPlayers().size()).isEqualTo(1);
        assertThat(UnoState.containsGame(game.getUuid())).isFalse();
    }

    @Test
    public void shouldFailAddPlayerCausedByInvalidLifecycle(){
        int playersBefore = game.getPlayers().size();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.addPlayer(game.getUuid(), "player name", false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldFailAddPlayerCausedByInvalidGameUuid(){
        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.addPlayer("invalid uuid", "player name", false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidLifecycle(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidGameUuid(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.removePlayer("invalid uuid", game.getPlayers().get(0).getUuid(), false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidPlayerUuid(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), "invalid uuid",false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailRemoveBotCausedByInvalidBotUuid(){
        prepareGame();

        addBot();

        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), "invalid uuid",true);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
    }

    private void prepareGame(){
        prepareGame(true);
    }

    private void prepareGameWithoutBot(){
        prepareGame(false);
    }

    private void prepareGame(boolean bot){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "", bot);
        playerService.addPlayer(game.getUuid(), "A Name", false);
    }

    private Player addBot(){
        Player bot = new Player("bot name", true);
        game.putBot(bot);
        return bot;
    }
}
