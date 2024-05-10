package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.exceptions.*;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.assertj.core.data.Percentage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.IllegalStateException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private GlobalStateService globalStateService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
        reset(globalStateService);
    }

    @Test
    public void shouldCreateGame(){
        String uuid = TestHelper.createGame(gameService).getUuid();

        assertThat(uuid).isNotNull();
        assertThat(UnoState.containsGame(uuid)).isTrue();
        verify(globalStateService, times(1)).saveGame(any());
    }

    @Test
    public void shouldTellTokenizedGameCreateFeatureDisabled(){
        boolean enabled = gameService.isTokenizedGameCreateFeatureEnabled();

        assertThat(enabled).isFalse();
    }

    @Test
    public void shouldTellTokenizedGameCreateFeatureEnabled(){
        given(tokenService.isFeatureEnabled()).willReturn(true);

        boolean enabled = gameService.isTokenizedGameCreateFeatureEnabled();

        assertThat(enabled).isTrue();
    }

    @Test
    public void shouldFailCreateGameCausedByInvalidToken(){
        UnoState.clear();
        doThrow(InvalidTokenException.class).when(tokenService).checkForTokenizedGameCreate(isA(String.class));
        Exception exception = null;
        String uuid = null;

        try {
            uuid = TestHelper.createGame(gameService).getUuid();
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(uuid).isNull();
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(InvalidTokenException.class);
        assertThat(UnoState.getGamesEntrySet()).isEmpty();
    }

    @Test
    public void shouldFailCreateGameCausedByIOException(){
        UnoState.clear();
        doThrow(FileReadException.class).when(tokenService).checkForTokenizedGameCreate(isA(String.class));
        Exception exception = null;
        String uuid = null;

        try {
            uuid = TestHelper.createGame(gameService).getUuid();
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(uuid).isNull();
        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(FileReadException.class);
        assertThat(UnoState.getGamesEntrySet()).isEmpty();
    }

    @Test
    public void shouldStartGame(){
        prepareGame();

        gameService.startGame(game.getUuid());

        assertStartedGameState();
        verify(globalStateService, times(1)).saveGame(any());
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

    @Test
    public void shouldAddMessage(){
        prepareGame();
        Player player = game.getPlayers().get(0);
        String testContent = "test content";

        gameService.addMessage(game.getUuid(), player.getUuid(), testContent);

        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.CHAT_MESSAGE);
        assertThat(game.getMessages().size()).isEqualTo(1);
        assertThat(game.getMessages().get(0)).isNotNull();
        assertThat(game.getMessages().get(0).getPlayerPublicUuid()).isEqualTo(player.getPublicUuid());
        assertThat(game.getMessages().get(0).getTime()).isCloseTo(System.currentTimeMillis(), Percentage.withPercentage(0.000001));
        assertThat(game.getMessages().get(0).getContent()).isEqualTo(testContent);
        verify(globalStateService, times(1)).saveGame(any());
    }

    @Test
    public void shouldFailAddMessageCausedByInvalidGameUuid(){
        prepareGame();
        Player player = game.getPlayers().get(0);
        String testContent = "test content";
        Exception exception = null;

        try {
            gameService.addMessage("invalid", player.getUuid(), testContent);
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_GAME.getValue());
        assertThat(game.getMessages()).isEmpty();
    }

    @Test
    public void shouldFailAddMessageCausedByInvalidPlayerUuid(){
        prepareGame();
        String testContent = "test content";
        Exception exception = null;

        try {
            gameService.addMessage(game.getUuid(), "", testContent);
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.NO_SUCH_PLAYER.getValue());
        assertThat(game.getMessages()).isEmpty();
    }

    @Test
    public void shouldFailAddMessageCausedByEmptyMessage(){
        prepareGame();
        Player player = game.getPlayers().get(0);
        Exception exception = null;

        try {
            gameService.addMessage(game.getUuid(), player.getUuid(), "");
        } catch (Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(EmptyArgumentException.class);
        assertThat(exception.getMessage()).isEqualTo(ExceptionMessage.EMPTY_CHAT_MESSAGE.getValue());
        assertThat(game.getMessages()).isEmpty();
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
