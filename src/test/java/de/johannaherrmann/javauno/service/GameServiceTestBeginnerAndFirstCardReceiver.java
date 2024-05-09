package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.fixed.Deck;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import de.johannaherrmann.javauno.helper.UnoRandom;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceTestBeginnerAndFirstCardReceiver {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @MockBean
    private TokenService tokenService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.createGame(gameService);
        prepareGame();
    }

    @After
    public void teardown(){
        UnoRandom.testModeEnabled = false;
    }

    @Test
    public void shouldSelectLastWinnerAsBeginner(){
        int expectedBeginner = 2;
        game.setLastWinner(expectedBeginner);

        gameService.startGame(game.getUuid());

        assertThat(game.getCurrentPlayerIndex()).isEqualTo(expectedBeginner);
    }

    @Test
    public void shouldSelectBeginnerRandomly(){
        UnoRandom.testModeEnabled = true;
        int expectedBeginner = UnoRandom.getRandom().nextInt(game.getPlayers().size());
        game.setLastWinner(-1);

        gameService.startGame(game.getUuid());

        assertThat(game.getCurrentPlayerIndex()).isEqualTo(expectedBeginner);
    }

    @Test
    public void shouldResetLastWinnerCausedByWinnerIsGone(){
        game.setLastWinner(1);

        playerService.removePlayer(game.getUuid(), game.getPlayers().get(1).getUuid(), false, false);

        assertThat(game.getLastWinner()).isEqualTo(-1);
    }

    @Test
    public void shouldSelectSecondPlayerAsFirstCardReceiverBecauseOthersWereAlready(){
       game.addPreviousFirstCardReceiver(game.getPlayers().get(0).getUuid());
       game.addPreviousFirstCardReceiver(game.getPlayers().get(2).getUuid());
       Stack<Card> deck = Deck.getShuffled();
       Stack<Card> originDeck = new Stack<>();
       originDeck.addAll(deck);

       gameService.giveCards(game, deck);

       for(int i = 0; i < 7; i++){
           assertThat(game.getPlayers().get(1).getCards().get(i)).isEqualTo(originDeck.pop());
           assertThat(game.getPlayers().get(2).getCards().get(i)).isEqualTo(originDeck.pop());
           assertThat(game.getPlayers().get(0).getCards().get(i)).isEqualTo(originDeck.pop());
       }
    }

    @Test
    public void shouldSelectFirstCardReceiverRandomly(){
        UnoRandom.testModeEnabled = true;
        int expectedFirstCardReceiver = UnoRandom.getRandom().nextInt(game.getPlayers().size());
        Stack<Card> deck = Deck.getShuffled();
        Stack<Card> originDeck = new Stack<>();
        originDeck.addAll(deck);

        gameService.giveCards(game, deck);

        assertThat(game.getPlayers().get(expectedFirstCardReceiver).getCards().get(0)).isEqualTo(originDeck.pop());
    }

    @Test
    public void shouldSelectFirstCardReceiverRandomlyButOnePlayerExcluded(){
        UnoRandom.testModeEnabled = true;
        Random random = UnoRandom.getRandom();
        int excluded = random.nextInt(game.getPlayers().size());
        game.addPreviousFirstCardReceiver(game.getPlayers().get(excluded).getUuid());
        int expectedFirstCardReceiver = random.nextInt(game.getPlayers().size());
        Stack<Card> deck = Deck.getShuffled();
        Stack<Card> originDeck = new Stack<>();
        originDeck.addAll(deck);

        gameService.giveCards(game, deck);

        assertThat(game.getPlayers().get(expectedFirstCardReceiver).getCards().get(0)).isEqualTo(originDeck.pop());
    }

    @Test
    public void shouldSelectFirstCardReceiverTotallyRandomlyAgainBecauseAllPlayersHaveBeenFirstReceiverAlready(){
        UnoRandom.testModeEnabled = true;
        int expectedFirstCardReceiver = UnoRandom.getRandom().nextInt(game.getPlayers().size());
        Stack<Card> deck = Deck.getShuffled();
        Stack<Card> originDeck = new Stack<>();
        originDeck.addAll(deck);
        game.addPreviousFirstCardReceiver(game.getPlayers().get(0).getUuid());
        game.addPreviousFirstCardReceiver(game.getPlayers().get(1).getUuid());
        game.addPreviousFirstCardReceiver(game.getPlayers().get(2).getUuid());

        gameService.giveCards(game, deck);

        assertThat(game.getPlayers().get(expectedFirstCardReceiver).getCards().get(0)).isEqualTo(originDeck.pop());
    }

    @Test
    public void shouldRemoveGonePlayerFromPreviousFirstCardReceiverList(){
        game.addPreviousFirstCardReceiver(game.getPlayers().get(0).getUuid());

        playerService.removePlayer(game.getUuid(), game.getPlayers().get(0).getUuid(), false, false);

        assertFalse(game.wasAlreadyFirstCardReceiver(game.getPlayers().get(0).getUuid()));
    }

    @Test
    public void shouldAddSelectedPlayerFromPreviousFirstCardReceiverList(){
        UnoRandom.testModeEnabled = true;
        int expectedFirstCardReceiver = UnoRandom.getRandom().nextInt(game.getPlayers().size());

        gameService.giveCards(game, Deck.getShuffled());

        assertTrue(game.wasAlreadyFirstCardReceiver(game.getPlayers().get(expectedFirstCardReceiver).getUuid()));
    }

    private void assertStartedGameState(){
        for(Player player : game.getPlayers()){
            assertThat(player.getCards().size()).isEqualTo(7);
        }
        assertThat(game.getDiscardPile().size()).isEqualTo(1);
        assertThat(game.getDrawPile().size()).isEqualTo(86);
        assertThat(game.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.STARTED_GAME);
        assertThat(game.getParty()).isEqualTo(1);
    }

    private void prepareGame(){
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "Lucy", false);
    }

}
