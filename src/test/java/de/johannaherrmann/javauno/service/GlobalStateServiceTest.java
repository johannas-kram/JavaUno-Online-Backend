package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GlobalStateServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GlobalStateService globalStateService;

    @MockBean
    private PersistenceService persistenceService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
        reset(persistenceService);
    }

    @Test
    public void shouldSaveGame() {
        long oldLastAction = game.getLastAction();
        game.setLastAction(oldLastAction - 100);
        TestHelper.deleteGames();

        globalStateService.saveGame(game);
        long newLastAction = game.getLastAction();

        assertThat(newLastAction).isGreaterThanOrEqualTo(oldLastAction);
        verify(persistenceService, times(1)).saveGame(game);
    }

    @Test
    public void shouldRemoveGame(){
        playerService.addPlayer(game.getUuid(), "i am a bot", true);

        globalStateService.removeGameIfNoHumans(game);

        assertThat(UnoState.containsGame(game.getUuid())).isFalse();
        verify(persistenceService, times(1)).deleteGame(game.getUuid());
    }

    @Test
    public void shouldRemoveOldGame(){
        game.setLastAction(System.currentTimeMillis()-(GlobalStateService.MAX_DURATION_WITHOUT_ACTION +100));

        globalStateService.removeOldGames();

        assertThat(UnoState.containsGame(game.getUuid())).isFalse();
        verify(persistenceService, times(1)).deleteGame(game.getUuid());
    }

    @Test
    public void shouldLoadGames() {
        Game game1 = TestHelper.prepareAndStartGame(gameService, playerService);
        Game game2 = TestHelper.prepareAndStartGame(gameService, playerService);
        List<Game> games = List.of(game1, game2);
        UnoState.clear();
        game1.setLastAction(100);
        when(persistenceService.loadGames()).thenReturn(games);
        Exception exception = null;
        Game loadedGame1 = null;
        Game loadedGame2 = null;

        try {
            globalStateService.loadGames();
            loadedGame1 = UnoState.getGame(game1.getUuid());
            loadedGame2 = UnoState.getGame(game2.getUuid());
        } catch (Exception ex) {
            exception = ex;
        }

        assertThat(exception).isNull();
        assertThat(loadedGame1).isNotNull();
        assertThat(loadedGame2).isNotNull();
        assertThat(loadedGame1.getUuid()).isEqualTo(game1.getUuid());
        assertThat(loadedGame2.getUuid()).isEqualTo(game2.getUuid());
        assertThat(loadedGame1.getLastAction()).isBetween(System.currentTimeMillis() - 100, System.currentTimeMillis());
    }
}
