package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.CardType;
import de.markherrmann.javauno.data.fixed.Color;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BotServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private RemainService remainService;

    private Game game;

    @Before
    public void setup(){
        String uuid = gameService.createGame();
        game = UnoState.getGame(uuid);
        addHumanAndBot();
        gameService.startGame(game.getUuid());
        game.setTurnState(TurnState.PUT_DRAWN);
    }

    @Test
    public void shouldMakeBotTurnPutOrDrawPut() throws Exception {
        Player player = game.getPlayers().get(1);
        player.addCard(game.getTopCard());

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5100);

        assertThat(game.getDiscardPile().size()).isEqualTo(2);
        assertThat(player.getCardCount()).isEqualTo(7);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnPutDrawnPut() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        game.getDrawPile().push(game.getTopCard());

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5600);

        assertThat(game.getDiscardPile().size()).isEqualTo(2);
        assertThat(game.getDrawPile().size()).isEqualTo(93);
        assertThat(player.getCardCount()).isEqualTo(0);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnPutOrDrawDraw() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5100);

        assertThat(game.getDrawPile().size()).isEqualTo(92);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getTurnState()).isEqualTo(TurnState.PUT_DRAWN);
    }

    @Test
    public void shouldMakeBotTurnPutDrawnRemain() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        game.getDrawPile().push(getNoneMatchingCard());

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5600);

        assertThat(game.getDiscardPile().size()).isEqualTo(1);
        assertThat(game.getDrawPile().size()).isEqualTo(93);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getTurnState()).isNotEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnDrawPenalties() throws Exception {
        Player player = game.getPlayers().get(1);
        player.setDrawPenalties(2);

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(3600);

        assertThat(game.getDrawPile().size()).isEqualTo(91);
        assertThat(player.getCardCount()).isEqualTo(9);
        assertThat(game.getTurnState()).isEqualTo(TurnState.PUT_OR_DRAW);
    }

    @Test
    public void shouldMakeBotTurnDrawDutiesDraw() throws Exception {
        Player player = game.getPlayers().get(1);
        game.setDrawDuties(2);
        player.getCards().clear();

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(3600);

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

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(3100);

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

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5600);

        assertThat(game.getDiscardPile().size()).isEqualTo(2);
        assertThat(player.getCardCount()).isEqualTo(1);
        assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
        assertThat(game.getDesiredColor()).isNotNull();
        if(player.getCards().get(0).isJokerCard()){
            assertThat(game.getDesiredColor()).isEqualTo(Color.RED.name());
        } else {
            assertThat(game.getDesiredColor()).isEqualTo(player.getCards().get(0).getColor());
        }
    }

    @Test
    public void shouldMakeBotTurnSayUno() throws Exception {
        Player player = game.getPlayers().get(1);
        player.getCards().clear();
        player.addCard(getNoneMatchingCard());
        player.addCard(game.getTopCard());

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(5600);
        
        assertThat(player.isUnoSaid()).isEqualTo(BotService.getLastSayUnoRandomNumber() < 8);
    }

    @Test
    public void shouldMakeBotTurnComplete() throws Exception {
        Player player = game.getPlayers().get(1);
        while (CardType.SKIP.equals(game.getTopCard().getCardType()) || CardType.REVERSE.equals(game.getTopCard().getCardType())){
            Card card = game.getDrawPile().pop();
            game.getDiscardPile().push(card);
        }
        player.addCard(game.getTopCard());
        int discardPileSize = game.getDiscardPile().size();

        remainService.remain(game.getUuid(), game.getPlayers().get(0).getUuid());
        Thread.sleep(8600);

        assertThat(game.getDiscardPile().size()).isEqualTo(discardPileSize+1);
        assertThat(player.getCardCount()).isEqualTo(7);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
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
