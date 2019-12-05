package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
        Card topCard = TestHelper.giveCardByString("NUMBER:BLUE:3");
        Card playersCard = TestHelper.giveCardByString("NUMBER:RED:3");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchBySkip(){
        Card topCard = TestHelper.giveCardByString("SKIP:BLUE");
        Card playersCard = TestHelper.giveCardByString("SKIP:YELLOW");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByReverse(){
        Card topCard = TestHelper.giveCardByString("REVERSE:GREEN");
        Card playersCard = TestHelper.giveCardByString("REVERSE:YELLOW");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByDraw2(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:GREEN");
        Card playersCard = TestHelper.giveCardByString("DRAW_2:YELLOW");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByColor(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:GREEN");
        Card playersCard = TestHelper.giveCardByString("SKIP:GREEN");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchByJoker(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:GREEN");
        Card playersCard = TestHelper.giveCardByString("JOKER");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchOnJokerByDesiredColor(){
        Card topCard = TestHelper.giveCardByString("JOKER");
        Card playersCard = TestHelper.giveCardByString("NUMBER:RED:0");
        game.setDesiredColor("RED");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchOnDraw4JokerByDesiredColor(){
        Card topCard = TestHelper.giveCardByString("DRAW_4");
        Card playersCard = TestHelper.giveCardByString("NUMBER:RED:0");
        game.setDesiredColor("RED");
        shouldPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMatchCausedByCumulativeDraw2(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:RED");
        Card playersCard = TestHelper.giveCardByString("DRAW_2:BLUE");
        shouldPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldMatchCausedByCumulativeDraw4(){
        Card topCard = TestHelper.giveCardByString("DRAW_4");
        Card playersCard = TestHelper.giveCardByString("DRAW_4");
        shouldPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldMatchCausedByDrawnCard(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:RED");
        Card playersCard = TestHelper.giveCardByString("JOKER");
        shouldPutCard(topCard, playersCard, TurnState.PUT_DRAWN);
    }
    
    @Test
    public void shouldNotMatchByMixedActionCards(){
        Card topCard = TestHelper.giveCardByString("SKIP:BLUE");
        Card playersCard = TestHelper.giveCardByString("REVERSE:RED");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchDraw2OnDraw4(){
        Card topCard = TestHelper.giveCardByString("DRAW_4");
        Card playersCard = TestHelper.giveCardByString("DRAW_2:RED");
        game.setDesiredColor("BLUE");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchOnJokerByUndesiredColor(){
        Card topCard = TestHelper.giveCardByString("JOKER");
        Card playersCard = TestHelper.giveCardByString("REVERSE:RED");
        game.setDesiredColor("BLUE");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchOnDraw4JokerByUndesiredColor(){
        Card topCard = TestHelper.giveCardByString("DRAW_4");
        Card playersCard = TestHelper.giveCardByString("REVERSE:RED");
        game.setDesiredColor("BLUE");
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldNotMatchCausedByNotCumulativeDraw2(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:RED");
        Card playersCard = TestHelper.giveCardByString("JOKER");
        shouldNotPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldNotMatchCausedByNotCumulativeDraw4(){
        Card topCard = TestHelper.giveCardByString("DRAW_4");
        Card playersCard = TestHelper.giveCardByString("JOKER");
        shouldNotPutCard(topCard, playersCard, TurnState.DRAW_DUTIES_OR_CUMULATIVE);
    }

    @Test
    public void shouldNotMatchCausedByNotDrawnCard(){
        Card topCard = TestHelper.giveCardByString("DRAW_2:RED");
        Card playersCard = TestHelper.giveCardByString("JOKER");
        game.getPlayers().get(0).addCard(topCard);
        shouldNotPutCard(topCard, playersCard, TurnState.PUT_DRAWN);
    }

    private void shouldPutCard(Card topCard, Card playersCard, TurnState turnState){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(turnState);
        game.getDiscardPile().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);
        int discardPileSize = game.getDiscardPile().size();
        Exception exception = null;

        try {
            putService.put(gameUuid, playerUuid, playersCard, 1);
        } catch (Exception ex){
            exception = ex;
        }

        TestHelper.assertPutCard(game, playersCard, discardPileSize, exception);
    }

    private void shouldNotPutCard(Card topCard, Card playersCard, TurnState turnState){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.setTurnState(turnState);
        game.getDiscardPile().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);
        Exception exception = new Exception("");

        try {
            putService.put(gameUuid, playerUuid, playersCard, 0);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotPutCard(game, playersCard, exception, turnState);
    }

    private void assertNotPutCard(Game game, Card card, Exception exception, TurnState turnState){
        assertThat(exception.getClass().getSimpleName()).isEqualTo("CardDoesNotMatchException");
        assertThat(game.getTopCard()).isNotEqualTo(card);
        assertThat(game.getPlayers().get(0).getCards()).isNotEmpty();
        assertThat(game.getPlayers().get(0).getCards().get(0)).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(turnState);
    }

}
