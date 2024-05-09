package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.GameLifecycle;
import de.johannaherrmann.javauno.data.state.component.TurnState;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;
import de.johannaherrmann.javauno.exceptions.IllegalStateException;
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
public class PutServiceGeneralFailTest {

    @Autowired
    private PutService putService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.prepareAndStartGame(gameService, playerService);
    }

    @After
    public void teardown(){
        TestHelper.deleteGames();
    }

    @Test
    public void shouldFailCausedByAnotherTurn(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(card);
        game.setCurrentPlayerIndex(1);
        Exception exception = new Exception("");

        try {
            putService.put(gameUuid, playerUuid, card, 0);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotPut(game, card);
        assertException(exception, IllegalStateException.class.getSimpleName(), ExceptionMessage.NOT_YOUR_TURN.getValue());
    }

    @Test
    public void shouldFailCausedByInvalidLifecycle(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(card);
        game.setCurrentPlayerIndex(1);
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
        Exception exception = new Exception("");

        try {
            putService.put(gameUuid, playerUuid, card, 0);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotPut(game, card);
        assertException(exception, IllegalStateException.class.getSimpleName(), ExceptionMessage.INVALID_STATE_GAME.getValue());
    }

    @Test
    public void shouldFailCausedByInvalidCard(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        Card wrongCard = TestHelper.findWrongCard(card, game);
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(wrongCard);
        Exception exception = new Exception("");

        try {
            putService.put(gameUuid, playerUuid, card, 0);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotPut(game, wrongCard);
        assertException(exception, IllegalArgumentException.class.getSimpleName(), ExceptionMessage.NO_SUCH_CARD.getValue());
    }

    private void assertNotPut(Game game, Card card){
        game.getDiscardPile().pop();
        assertThat(game.getDiscardPile()).isEmpty();
        assertThat(game.getPlayers().get(0).getCards()).isNotEmpty();
        assertThat(game.getPlayers().get(0).getCards().get(0)).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(TurnState.PUT_OR_DRAW);
    }

    private void assertException(Exception exception, String exceptionType, String message){
        assertThat(exception.getClass().getSimpleName()).isEqualTo(exceptionType);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

}
