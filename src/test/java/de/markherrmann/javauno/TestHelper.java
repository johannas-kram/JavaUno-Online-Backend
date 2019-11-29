package de.markherrmann.javauno;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.service.GameService;
import de.markherrmann.javauno.service.PlayerService;

import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHelper {

    public static Game prepareAndStartGame(GameService gameService, PlayerService playerService){
        String uuid = gameService.createGame();
        Game game = UnoState.getGame(uuid);
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "Jana", false);
        playerService.addPlayer(game.getUuid(), "A Name", false);
        gameService.startGame(game.getUuid());
        return game;
    }

    public static void assertPutCard(Game game, Card card, int discadrPileSize, String result){
        assertThat(result).isEqualTo("success");
        assertThat(game.getPlayers().get(0).getCards()).isEmpty();
        assertThat(game.getDiscardPile().size()).isEqualTo(discadrPileSize+1);
        assertThat(game.getTopCard()).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(TurnState.FINAL_COUNTDOWN);
    }

    public static Card findWrongCard(Card rightCard, Game game){
        Card card = rightCard;
        while(card.equals(rightCard)){
            card = game.getDrawPile().pop();
        }
        return card;
    }

    public static Card giveCardByString(String cardString){
        Stack<Card> cards = Deck.getShuffled();
        for(Card card : cards){
            if(card.toString().equals(cardString)){
                return card;
            }
        }
        return null;
    }
}
