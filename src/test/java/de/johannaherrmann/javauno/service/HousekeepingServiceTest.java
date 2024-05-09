package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HousekeepingServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private HousekeepingService housekeepingService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
    }

    @Test
    public void shouldUpdateLastAction() throws Exception {
        long oldLastAction = game.getLastAction();
        Thread.sleep(3000);

        housekeepingService.updateLastAction(game);
        long newLastAction = game.getLastAction();

        assertThat(newLastAction).isGreaterThan(oldLastAction);
    }

    @Test
    public void shouldRemoveGame(){
        playerService.addPlayer(game.getUuid(), "i am a bot", true);

        housekeepingService.removeGameIfNoHumans(game);

        assertThat(UnoState.containsGame(game.getUuid())).isFalse();
        assertThat(new File("./data/games/" + game.getUuid())).doesNotExist();
    }

    @Test
    public void shouldRemoveOldGame(){
        game.setLastAction(System.currentTimeMillis()-(HousekeepingService.MAX_DURATION_WITHOUT_ACTION +100));

        housekeepingService.removeOldGames();

        assertThat(UnoState.containsGame(game.getUuid())).isFalse();
    }


}
