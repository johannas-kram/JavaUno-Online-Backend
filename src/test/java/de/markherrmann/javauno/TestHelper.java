package de.markherrmann.javauno;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.service.GameService;
import de.markherrmann.javauno.service.PlayerService;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;

import java.util.Stack;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHelper {

    public static Game prepareAndStartGame(GameService gameService, PlayerService playerService){
        Game game = createGame(gameService);
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "Jana", false);
        playerService.addPlayer(game.getUuid(), "A Name", false);
        gameService.startGame(game.getUuid());
        return game;
    }

    public static Game createGame(GameService gameService){
        String uuid = gameService.createGame("empty");
        Game game = UnoState.getGame(uuid);;
        game.setLastWinner(0);
        return game;
    }

    public static void assertPutCard(Game game, Card card, int discardPileSize, Exception exception){
        assertThat(exception).isNull();
        assertThat(game.getPlayers().get(0).getCards().size()).isEqualTo(1);
        assertThat(game.getDiscardPile().size()).isEqualTo(discardPileSize+1);
        assertThat(game.getTopCard()).isEqualTo(card);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
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

    public static GeneralResponse jsonToObject(final String json) {
        try {
            return new ObjectMapper().readValue(json, GeneralResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
