package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HousekeepingServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private HousekeepingService housekeepingService;

    private Game game;

    @Before
    public void setup(){
        String uuid = gameService.createGame();
        game = UnoState.getGames().get(uuid);
    }

    @Test
    public void shouldUpdateLastAction() throws Exception {
        long oldLastAction = game.getLastAction();
        Thread.sleep(3000);

        housekeepingService.updateGameLastAction(game);
        long newLastAction = game.getLastAction();

        assertThat(newLastAction).isGreaterThan(oldLastAction);
    }

    @Test
    public void shouldRemoveGame(){
        housekeepingService.removeGame(game.getUuid());

        assertThat(UnoState.getGames().containsKey(game.getUuid())).isFalse();
    }

    @Test
    public void shouldRemoveOldGame(){
        game.setLastAction(System.currentTimeMillis()-(HousekeepingService.MAX_DURATION_WITHOUT_ACTION+1));

        housekeepingService.removeOldGames();

        assertThat(UnoState.getGames().containsKey(game.getUuid())).isFalse();
    }
}
