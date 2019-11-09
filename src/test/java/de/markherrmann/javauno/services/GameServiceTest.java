package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    private Game game;

    @Before
    public void setup(){
        String uuid = gameService.createGame();
        game = UnoState.getGames().get(uuid);
    }

    @Test
    public void shouldCreateGame(){
        String uuid = gameService.createGame();

        assertThat(uuid).isNotNull();
        assertThat(UnoState.getGames()).containsKey(uuid);
    }

    @Test
    public void shouldAddPlayer(){
        int playersBefore = game.getPlayer().size();

        gameService.addPlayer(game.getUuid(), "player name", false);

        int playersNow = game.getPlayer().size();
        assertThat(playersNow-playersBefore).isEqualTo(1);
    }

    @Test
    public void shouldStartGame(){
        prepareGame();

        gameService.startGame(game.getUuid());

        assertStartedGameState();
    }

    @Test
    public void shouldGetOwnCards(){
        String uuid = gameService.createGame();
        game = UnoState.getGames().get(uuid);
        prepareGame();
        gameService.startGame(game.getUuid());

        List<Card> ownCards = gameService.getOwnCards(game.getUuid(), game.getPlayerList().get(0).getUuid());

        assertThat(ownCards.size()).isEqualTo(7);
    }

    private void assertStartedGameState(){
        for(Player player : game.getPlayerList()){
            assertThat(player.getCards().size()).isEqualTo(7);
        }
        assertThat(game.getLayStack().size()).isEqualTo(1);
        assertThat(game.getTakeStack().size()).isEqualTo(86);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
    }

    private void prepareGame(){
        gameService.addPlayer(game.getUuid(), "Max", false);
        gameService.addPlayer(game.getUuid(), "Maria", false);
        gameService.addPlayer(game.getUuid(), "", true);
    }

}
