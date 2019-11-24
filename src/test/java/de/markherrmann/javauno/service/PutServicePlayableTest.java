package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
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
public class PutServicePlayableTest {

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
        game.getDiscardPile().clear();
        game.getPlayers().get(0).clearCards();
    }

    @Test
    public void shouldMatchByNumber(){
        Card topCard = giveCardByString("NUMBER:BLUE:3");
        Card playersCard = giveCardByString("NUMBER:RED:3");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchBySkip(){
        Card topCard = giveCardByString("SKIP:BLUE");
        Card playersCard = giveCardByString("SKIP:YELLOW");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByReverse(){
        Card topCard = giveCardByString("REVERSE:GREEN");
        Card playersCard = giveCardByString("REVERSE:YELLOW");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByDraw2(){
        Card topCard = giveCardByString("DRAW_2:GREEN");
        Card playersCard = giveCardByString("DRAW_2:YELLOW");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByColor(){
        Card topCard = giveCardByString("DRAW_2:GREEN");
        Card playersCard = giveCardByString("SKIP:GREEN");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByJoker(){
        Card topCard = giveCardByString("DRAW_2:GREEN");
        Card playersCard = giveCardByString("JOKER");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchOnJokerByDesiredColor(){
        Card topCard = giveCardByString("JOKER");
        Card playersCard = giveCardByString("NUMBER:RED:0");
        game.setDesiredColor("RED");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchOnDraw4JokerByDesiredColor(){
        Card topCard = giveCardByString("DRAW_4");
        Card playersCard = giveCardByString("NUMBER:RED:0");
        game.setDesiredColor("RED");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchCausedByCumulativeDraw2(){
        Card topCard = giveCardByString("DRAW_2:RED");
        Card playersCard = giveCardByString("DRAW_2:BLUE");
        shouldPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldMatchCausedByCumulativeDraw4(){
        Card topCard = giveCardByString("DRAW_4");
        Card playersCard = giveCardByString("DRAW_4");
        shouldPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldMatchCausedByDrawnCard(){
        Card topCard = giveCardByString("DRAW_2:RED");
        Card playersCard = giveCardByString("JOKER");
        shouldPutCard(topCard, playersCard, TurnState.PUT_DRAWN);
    }
    
    @Test
    public void shouldNotMatchByMixedActionCards(){
        Card topCard = giveCardByString("SKIP:BLUE");
        Card playersCard = giveCardByString("REVERSE:RED");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchDraw2OnDraw4(){
        Card topCard = giveCardByString("DRAW_4");
        Card playersCard = giveCardByString("DRAW_2:RED");
        game.setDesiredColor("BLUE");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchOnJokerByUndesiredColor(){
        Card topCard = giveCardByString("JOKER");
        Card playersCard = giveCardByString("REVERSE:RED");
        game.setDesiredColor("BLUE");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchOnDraw4JokerByUndesiredColor(){
        Card topCard = giveCardByString("DRAW_4");
        Card playersCard = giveCardByString("REVERSE:RED");
        game.setDesiredColor("BLUE");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchCausedByNotCumulativeDraw2(){
        Card topCard = giveCardByString("DRAW_2:RED");
        Card playersCard = giveCardByString("JOKER");
        shouldNotPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldNotMatchCausedByNotCumulativeDraw4(){
        Card topCard = giveCardByString("DRAW_4");
        Card playersCard = giveCardByString("JOKER");
        shouldNotPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldNotMatchCausedByNotDrawnCard(){
        Card topCard = giveCardByString("DRAW_2:RED");
        Card playersCard = giveCardByString("JOKER");
        game.getPlayers().get(0).addCard(topCard);
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_DRAWN);
    }

    private void shouldPutCard(Card topCard, Card playersCard, TurnState turnState){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(turnState);
        game.getDiscardPile().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);

        String result = putService.put(gameUuid, playerUuid, playersCard, 0);

        TestHelper.assertPutCard(game, playersCard, result);
    }

    private void shouldNotPutCard(Card topCard, Card playersCard, TurnState turnState){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(turnState);
        game.getDiscardPile().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);


        String result = putService.put(gameUuid, playerUuid, playersCard, 0);

        assertNotPutCard(game, playersCard, result, turnState);
    }

    private void assertNotPutCard(Game game, Card card, String result, TurnState turnState){
        assertThat(result).isEqualTo("failure: card does not match.");
        assertThat(game.getTopCard()).isNotEqualTo(card);
        assertThat(game.getPlayers().get(0).getCards()).isNotEmpty();
        assertThat(game.getPlayers().get(0).getCards().get(0)).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(turnState);
    }

    private Card giveCardByString(String cardString){
        Stack<Card> cards = Deck.getShuffled();
        for(Card card : cards){
            if(card.toString().equals(cardString)){
                return card;
            }
        }
        return null;
    }

}
