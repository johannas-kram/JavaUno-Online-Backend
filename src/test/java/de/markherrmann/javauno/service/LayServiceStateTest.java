package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LayServiceStateTest {

    @Autowired
    private LayService layService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = LayServiceTestHelper.prepareGame(gameService, playerService);
    }

    @Test
    public void shouldLayCardInLayOrTakeState(){
        shouldLayCard();
    }

    @Test
    public void shouldLayCardInLayTakenState(){
        game.setTurnState(TurnState.LAY_TAKEN);
        shouldLayCard();
    }

    @Test
    public void shouldLayCardInTakeDutiesOrCumulateState(){
        game.setTurnState(TurnState.TAKE_DUTIES_OR_CUMULATE);
        game.getLayStack().push(findTake2Card());
        shouldLayCard();
    }

    @Test
    public void shouldFailCausedByInvalidStateTakeDuties(){
        game.setTurnState(TurnState.TAKE_DUTIES);
        shouldFailCausedByInvalidState();
    }

    @Test
    public void shouldFailCausedByInvalidStateSelectColor(){
        game.setTurnState(TurnState.SELECT_COLOR);
        shouldFailCausedByInvalidState();
    }

    @Test
    public void shouldFailCausedByInvalidStateFinalCountdown(){
        game.setTurnState(TurnState.FINAL_COUNTDOWN);
        shouldFailCausedByInvalidState();
    }

    private void shouldLayCard(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(card);

        String result = layService.lay(gameUuid, playerUuid, card, 0);

        LayServiceTestHelper.assertLaidCard(game, card, result);
    }



    private void shouldFailCausedByInvalidState(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(card);
        Exception exception = new Exception("");
        String result = "";
        TurnState turnState = game.getTurnState();

        try {
            result = layService.lay(gameUuid, playerUuid, card, 0);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotLaid(game, card, result, exception, "IllegalStateException", "Turn is in wrong state for this action.", turnState);
    }

    private void assertLaid(Game game, Card card, String result){
        LayServiceTestHelper.assertLaidCard(game, card, result);
        game.getLayStack().pop();
        assertThat(game.getTopCard()).isEqualTo(card);
    }

    private void assertNotLaid(Game game, Card card, String result, Exception exception, String exceptionType, String message, TurnState turnState){
        assertThat(result).isEqualTo("");
        game.getLayStack().pop();
        assertThat(game.getLayStack()).isEmpty();
        assertThat(game.getPlayers().get(0).getCards()).isNotEmpty();
        assertThat(game.getPlayers().get(0).getCards().get(0)).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(turnState);
        assertException(exception, exceptionType, message);
    }

    private void assertException(Exception exception, String exceptionType, String message){
        assertThat(exception.getClass().getSimpleName()).isEqualTo(exceptionType);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    private Card findTake2Card(){
        Stack<Card> cards = Deck.getShuffled();
        for(Card card : cards){
            if(card.isTakeCard() && card.getTake() == 2){
                return card;
            }
        }
        return null;
    }
}
