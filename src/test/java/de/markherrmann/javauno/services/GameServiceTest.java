package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameState;
import de.markherrmann.javauno.data.state.components.Player;
import de.markherrmann.javauno.data.state.components.RoundState;
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
    public void shouldGetRoundState(){
        prepareGame();
        Player me = game.getPlayerList().get(0);
        Player another = game.getPlayerList().get(1);
        gameService.startGame(game.getUuid());

        RoundState roundStateForMe = gameService.getBeginningState(game.getUuid(), me.getUuid());
        RoundState roundStateForAnother = gameService.getBeginningState(game.getUuid(), another.getUuid());

        assertRoundState(roundStateForMe, roundStateForAnother);
    }

    private void assertStartedGameState(){
        for(Player player : game.getPlayerList()){
            assertThat(player.getCards().size()).isEqualTo(7);
        }
        assertThat(game.getLayStack().size()).isEqualTo(1);
        assertThat(game.getTakeStack().size()).isEqualTo(86);
        assertThat(game.getGameState()).isEqualTo(GameState.IN_ROUND);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
    }

    private void assertRoundState(RoundState roundStateForMe, RoundState roundStateForAnother){
        for(int cardsCount : roundStateForMe.getPlayerCardsCount()){
            assertThat(cardsCount).isEqualTo(7);
        }
        assertThat(roundStateForMe.getOwnCards().size()).isEqualTo(7);
        assertThat(roundStateForMe.isOwnTurn()).isTrue();
        assertThat(roundStateForMe.getOwnCards().get(0)).isNotEqualTo(roundStateForAnother.getOwnCards().get(0));
        assertThat(roundStateForAnother.isOwnTurn()).isFalse();
    }

    private void prepareGame(){
        gameService.stateAddPlayer(game.getUuid());
        gameService.addPlayer(game.getUuid(), "Max", false);
        gameService.addPlayer(game.getUuid(), "Maria", false);
        gameService.addPlayer(game.getUuid(), "", true);
    }

}
