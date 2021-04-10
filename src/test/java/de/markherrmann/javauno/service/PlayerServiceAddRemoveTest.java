package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.fixed.CardType;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.ExceptionMessage;
import de.markherrmann.javauno.exceptions.IllegalStateException;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PlayerServiceAddRemoveTest {

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
    public void shouldAddPlayer(){
        int playersBefore = game.getPlayers().size();

        playerService.addPlayer(game.getUuid(), "player name", false);

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(1);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.ADDED_PLAYER);
    }

    @Test
    public void shouldRemovePlayer(){
        prepareGame();
        game.setCurrentPlayerIndex(2);
        int playersBefore = game.getPlayers().size();

        playerService.removePlayer(game.getUuid(), game.getPlayers().get(1).getUuid(), false, false);
        int playersNow = game.getPlayers().size();

        assertThat(playersBefore).isEqualTo(4);
        assertThat(playersNow).isEqualTo(3);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(1);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REMOVED_PLAYER);
    }

    @Test
    public void shouldRemoveBot_NotInGame(){
        prepareGame();
        Player bot = addBot();
        bot.getCards().clear();
        bot.getCards().add(TestHelper.giveCardByString("JOKER"));
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);

        int playersBefore = game.getPlayers().size();

        playerService.removePlayer(game.getUuid(), bot.getKickUuid(), true, false);
        int playersNow = game.getPlayers().size();

        assertThat(playersBefore).isEqualTo(5);
        assertThat(playersNow).isEqualTo(4);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REMOVED_PLAYER);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.SET_PLAYERS);
        assertThat(bot.getCards().get(0).getCardType()).isEqualTo(CardType.JOKER);
    }

    @Test
    public void shouldRemoveBot_InGame(){
        prepareGame();
        Player bot = addBot();
        bot.getCards().clear();
        bot.getCards().add(TestHelper.giveCardByString("JOKER"));
        game.setGameLifecycle(GameLifecycle.RUNNING);

        int playersBefore = game.getPlayers().size();

        playerService.removePlayer(game.getUuid(), bot.getKickUuid(), true, true);
        int playersNow = game.getPlayers().size();

        assertThat(playersBefore).isEqualTo(5);
        assertThat(playersNow).isEqualTo(4);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REMOVED_PLAYER);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.RUNNING);
        assertThat(game.getDrawPile().get(0).getCardType()).isEqualTo(CardType.JOKER);
    }

    @Test
    public void shouldRemoveGameCausedByNoHumanPlayers(){
        prepareGameWithoutBot();
        addBot();

        for(int i = 0; i <= 3; i++){
            playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false, false);
        }

        assertThat(game.getPlayers().size()).isEqualTo(1);
        assertThat(UnoState.containsGame(game.getUuid())).isFalse();
    }

    @Test
    public void shouldStopPartyCausedByOnlyOneRemainingPlayer(){
        prepareGame();
        game.getHumans().clear();
        game.getBots().clear();
        game.getPlayers().clear();
        addHuman();
        Player bot = addBot();
        game.setGameLifecycle(GameLifecycle.RUNNING);

        playerService.removePlayer(game.getUuid(), bot.getKickUuid(), true, true);
        int playersNow = game.getPlayers().size();

        assertThat(playersNow).isEqualTo(1);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.REMOVED_PLAYER);
        assertThat(game.getGameLifecycle()).isEqualTo(GameLifecycle.SET_PLAYERS);
        assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
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
        assertThat(playersNow).isEqualTo(playersBefore);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
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
        assertThat(playersNow).isEqualTo(playersBefore);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailAddPlayerCausedByLimitReached(){
        for(int i = 1; i <= 10; i++){
            playerService.addPlayer(game.getUuid(), "player name", false);
        }
        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.addPlayer(game.getUuid(), "player name", false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow).isEqualTo(playersBefore);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.PLAYERS_LIMIT_REACHED.getValue());
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidLifecycle_NotInGame(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false, false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidLifecycle_InGame(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false, true);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
    }

    @Test
    public void shouldFailRemovePlayerInGameCausedByItsTheirTurn(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        game.setGameLifecycle(GameLifecycle.RUNNING);
        Exception exception = null;
        game.setCurrentPlayerIndex(0);

        try {
            playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false, true);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalStateException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.INVALID_STATE_GAME.getValue());
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidGameUuid(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.removePlayer("invalid uuid", game.getPlayers().get(0).getUuid(), false, false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
    }

    @Test
    public void shouldFailRemovePlayerCausedByInvalidPlayerUuid(){
        prepareGame();
        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), "invalid uuid",false, false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_PLAYER.getValue());
    }

    @Test
    public void shouldFailRemoveBotCausedByInvalidBotUuid(){
        prepareGame();

        addBot();

        int playersBefore = game.getPlayers().size();
        Exception exception = null;

        try {
            playerService.removePlayer(game.getUuid(), "invalid uuid",true, false);
        } catch(Exception ex){
            exception = ex;
        }

        int playersNow = game.getPlayers().size();
        assertThat(playersNow-playersBefore).isEqualTo(0);
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_PLAYER.getValue());
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

    private Player addHuman(){
        Player human = new Player("human name", true);
        game.putHuman(human);
        return human;
    }
}
