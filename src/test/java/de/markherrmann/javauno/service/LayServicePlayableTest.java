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
public class LayServicePlayableTest {

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
        game.getLayStack().clear();
        game.getPlayers().get(0).clearCards();
    }

    @Test
    public void shouldMatchByNumber(){
        Card topCard = giveCardByString("NUMBER:BLUE:3");
        Card playersCard = giveCardByString("NUMBER:RED:3");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchBySkip(){
        Card topCard = giveCardByString("SKIP:BLUE");
        Card playersCard = giveCardByString("SKIP:YELLOW");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchByRetour(){
        Card topCard = giveCardByString("RETOUR:GREEN");
        Card playersCard = giveCardByString("RETOUR:YELLOW");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchByTake2(){
        Card topCard = giveCardByString("TAKE2:GREEN");
        Card playersCard = giveCardByString("TAKE2:YELLOW");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchByColor(){
        Card topCard = giveCardByString("TAKE2:GREEN");
        Card playersCard = giveCardByString("SKIP:GREEN");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchByJoker(){
        Card topCard = giveCardByString("TAKE2:GREEN");
        Card playersCard = giveCardByString("JOKER");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchOnJokerByDesiredColor(){
        Card topCard = giveCardByString("JOKER");
        Card playersCard = giveCardByString("NUMBER:RED:0");
        game.setDesiredColor("RED");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchOnTake4JokerByDesiredColor(){
        Card topCard = giveCardByString("TAKE4");
        Card playersCard = giveCardByString("NUMBER:RED:0");
        game.setDesiredColor("RED");
        shouldLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldMatchCausedByCumulativeTake2(){
        Card topCard = giveCardByString("TAKE2:RED");
        Card playersCard = giveCardByString("TAKE2:BLUE");
        shouldLayCard(topCard, playersCard, TurnState.TAKE_DUTIES_OR_CUMULATE);
    }

    @Test
    public void shouldMatchCausedByCumulativeTake4(){
        Card topCard = giveCardByString("TAKE4");
        Card playersCard = giveCardByString("TAKE4");
        shouldLayCard(topCard, playersCard, TurnState.TAKE_DUTIES_OR_CUMULATE);
    }

    @Test
    public void shouldMatchCausedByTakenCard(){
        Card topCard = giveCardByString("TAKE2:RED");
        Card playersCard = giveCardByString("JOKER");
        shouldLayCard(topCard, playersCard, TurnState.LAY_TAKEN);
    }
    
    @Test
    public void shouldNotMatchByMixedActions(){
        Card topCard = giveCardByString("SKIP:BLUE");
        Card playersCard = giveCardByString("RETOUR:RED");
        shouldNotLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldNotMatchTake2OnTake4(){
        Card topCard = giveCardByString("TAKE4");
        Card playersCard = giveCardByString("TAKE2:RED");
        game.setDesiredColor("BLUE");
        shouldNotLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldNotMatchOnJokerByUndesiredColor(){
        Card topCard = giveCardByString("JOKER");
        Card playersCard = giveCardByString("RETOUR:RED");
        game.setDesiredColor("BLUE");
        shouldNotLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldNotMatchOnTake4JokerByUndesiredColor(){
        Card topCard = giveCardByString("TAKE4");
        Card playersCard = giveCardByString("RETOUR:RED");
        game.setDesiredColor("BLUE");
        shouldNotLayCard(topCard, playersCard, TurnState.LAY_OR_TAKE);
    }

    @Test
    public void shouldNotMatchCausedByNotCumulativeTake2(){
        Card topCard = giveCardByString("TAKE2:RED");
        Card playersCard = giveCardByString("JOKER");
        shouldNotLayCard(topCard, playersCard, TurnState.TAKE_DUTIES_OR_CUMULATE);
    }

    @Test
    public void shouldNotMatchCausedByNotCumulativeTake4(){
        Card topCard = giveCardByString("TAKE4");
        Card playersCard = giveCardByString("JOKER");
        shouldNotLayCard(topCard, playersCard, TurnState.TAKE_DUTIES_OR_CUMULATE);
    }

    @Test
    public void shouldNotMatchCausedByNotTakenCard(){
        Card topCard = giveCardByString("TAKE2:RED");
        Card playersCard = giveCardByString("JOKER");
        game.getPlayers().get(0).addCard(topCard);
        shouldNotLayCard(topCard, playersCard, TurnState.LAY_TAKEN);
    }

    private void shouldLayCard(Card topCard, Card playersCard, TurnState turnState){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(turnState);
        game.getLayStack().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);

        String result = layService.lay(gameUuid, playerUuid, playersCard.toString(), 0);

        LayServiceTestHelper.assertLaidCard(game, playersCard, result);
    }

    private void shouldNotLayCard(Card topCard, Card playersCard, TurnState turnState){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(turnState);
        game.getLayStack().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);


        String result = layService.lay(gameUuid, playerUuid, playersCard.toString(), 0);

        assertNotLaidCard(game, playersCard, result, turnState);
    }

    private void assertNotLaidCard(Game game, Card card, String result, TurnState turnState){
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
