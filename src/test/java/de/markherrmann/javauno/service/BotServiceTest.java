package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.CardType;
import de.markherrmann.javauno.data.fixed.Color;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
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
public class BotServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private KeepService keepService;

    @Autowired
    private TurnService turnService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
        addHumanAndBot();
        gameService.startGame(game.getUuid());
        game.setTurnState(TurnState.PUT_DRAWN);
    }

    @Test
    public void shouldMakeBotTurnPutOrDrawPut() throws Exception {
        Player player = game.getPlayers().get(1);
        player.addCard(game.getTopCard());

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(2100);

        assertThat(game.getDiscardPile().size()).isEqualTo(2);
        assertThat(player.getCardCount()).isEqualTo(7);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.PUT_OR_DRAW);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    @Test
    public void shouldMakeBotTurnPutDrawnPut() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        game.getDrawPile().push(game.getTopCard());

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(2600);

        assertThat(game.getDiscardPile().size()).isEqualTo(2);
        assertThat(game.getDrawPile().size()).isEqualTo(93);
        assertThat(player.getCardCount()).isEqualTo(0);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.PUT_OR_DRAW);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.FINISHED_GAME);
    }

    @Test
    public void shouldMakeBotTurnPutOrDrawDraw() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(2100);

        assertThat(game.getDrawPile().size()).isEqualTo(92);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getTurnState()).isEqualTo(TurnState.PUT_DRAWN);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.DRAWN_CARD);
    }

    @Test
    public void shouldMakeBotTurnPutDrawnKeep() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        game.getDrawPile().push(getNoneMatchingCard());

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(2600);

        assertThat(game.getDiscardPile().size()).isEqualTo(1);
        assertThat(game.getDrawPile().size()).isEqualTo(93);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnDrawPenalties() throws Exception {
        Player player = game.getPlayers().get(1);
        player.setDrawPenalties(2);

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(600);

        assertThat(game.getDrawPile().size()).isEqualTo(91);
        assertThat(player.getCardCount()).isEqualTo(9);
        assertThat(game.getTurnState()).isEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnDrawDutiesDraw() throws Exception {
        Player player = game.getPlayers().get(1);
        game.setDrawDuties(2);
        player.getCards().clear();

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(600);

        assertThat(game.getDrawPile().size()).isEqualTo(91);
        assertThat(player.getCardCount()).isEqualTo(2);
        assertThat(game.getTurnState()).isEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnDrawDutiesCumulative() throws Exception {
        Player player = game.getPlayers().get(1);
        game.setDrawDuties(2);
        game.getDiscardPile().push(getDraw2Card());
        player.addCard(getDraw2Card());

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(100);

        assertThat(game.getDiscardPile().size()).isEqualTo(3);
        assertThat(player.getCardCount()).isEqualTo(7);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.DRAW_DUTIES);
    }

    @Test
    public void shouldMakeBotTurnSelectColor() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        player.addCard(getNoneMatchingCard());
        player.addCard(getJoker());

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(2600);

        assertThat(game.getDiscardPile().size()).isEqualTo(2);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
        assertThat(game.getDesiredColor()).isNotNull();
        if(player.getCards().get(0).isJokerCard()){
            assertThat(game.getDesiredColor()).isEqualTo(Color.RED.name());
        } else {
            assertThat(game.getDesiredColor()).isEqualTo(player.getCards().get(0).getColor());
        }
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SELECTED_COLOR);
    }

    @Test
    public void shouldMakeBotTurnSayUno() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        player.addCard(getNoneMatchingCard());
        player.addCard(game.getTopCard());

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(2600);

        boolean shouldSayUno = BotService.getLastSayUnoRandomNumber() < 9;
        assertThat(player.isUnoSaid()).isEqualTo(shouldSayUno);
        if(shouldSayUno){
            assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.SAID_UNO);
        }
    }

    @Test
    public void shouldMakeBotTurnComplete() throws Exception {
        Player player = game.getPlayers().get(1);
        while (CardType.SKIP.equals(game.getTopCard().getCardType()) || CardType.REVERSE.equals(game.getTopCard().getCardType())){
            Card card = game.getDrawPile().pop();
            game.getDiscardPile().push(card);
        }
        player.getCards().clear();
        player.addCard(game.getTopCard());
        player.addCard(game.getTopCard());
        int discardPileSize = game.getDiscardPile().size();

        keepService.keep(game.getUuid(), game.getPlayers().get(0).getUuid());
        turnService.next(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5600);

        assertThat(game.getDiscardPile().size()).isEqualTo(discardPileSize+1);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.NEXT_TURN);
    }

    private Card getNoneMatchingCard(){
        Stack<Card> deck = Deck.getShuffled();
        for(Card card : deck){
            if(!PutService.isMatch(game, card)){
                return card;
            }
        }
        return null;
    }

    private Card getJoker(){
        Stack<Card> deck = Deck.getShuffled();
        for(Card card : deck){
            if(card.isJokerCard()){
                return card;
            }
        }
        return null;
    }

    private Card getDraw2Card(){
        Stack<Card> deck = Deck.getShuffled();
        for(Card card : deck){
            if(card.getDrawValue() == 2){
                return card;
            }
        }
        return null;
    }

    private void addHumanAndBot(){
        Player player = new Player("player name", false);
        Player player2 = new Player("player2 name", true);
        game.putHuman(player);
        game.putBot(player2);
    }

}
