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
public class PutServiceStateTest {

    @Autowired
    private PutService putService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = PutServiceTestHelper.prepareGame(gameService, playerService);
    }

    @Test
    public void shouldPutCardInPutOrDrawState(){
        shouldPutCard();
    }

    @Test
    public void shouldPutCardInPutDrawnState(){
        game.setTurnState(TurnState.PUT_DRAWN);
        shouldPutCard();
    }

    @Test
    public void shouldPutCardInDrawDutiesOrCumulativeState(){
        game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
        game.getDiscardPile().push(findDraw2Card());
        shouldPutCard();
    }

    @Test
    public void shouldFailCausedByInvalidStateDrawDuties(){
        game.setTurnState(TurnState.DRAW_DUTIES);
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

    private void shouldPutCard(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(card);

        String result = putService.put(gameUuid, playerUuid, card.toString(), 0);

        PutServiceTestHelper.assertPutCard(game, card, result);
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
            result = putService.put(gameUuid, playerUuid, card.toString(), 0);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotPut(game, card, result, exception, "IllegalStateException", "Turn is in wrong state for this action.", turnState);
    }

    private void assertNotPut(Game game, Card card, String result, Exception exception, String exceptionType, String message, TurnState turnState){
        assertThat(result).isEqualTo("");
        game.getDiscardPile().pop();
        assertThat(game.getDiscardPile()).isEmpty();
        assertThat(game.getPlayers().get(0).getCards()).isNotEmpty();
        assertThat(game.getPlayers().get(0).getCards().get(0)).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(turnState);
        assertException(exception, exceptionType, message);
    }

    private void assertException(Exception exception, String exceptionType, String message){
        assertThat(exception.getClass().getSimpleName()).isEqualTo(exceptionType);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    private Card findDraw2Card(){
        Stack<Card> cards = Deck.getShuffled();
        for(Card card : cards){
            if(card.getDrawValue() == 2){
                return card;
            }
        }
        return null;
    }
}
