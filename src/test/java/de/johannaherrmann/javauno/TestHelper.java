package de.johannaherrmann.javauno;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.johannaherrmann.javauno.controller.response.GeneralResponse;
import de.johannaherrmann.javauno.controller.response.PutCardResponse;
import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.fixed.Deck;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.service.GameService;
import de.johannaherrmann.javauno.service.PlayerService;
import de.johannaherrmann.javauno.service.push.PushMessage;
import de.johannaherrmann.javauno.service.push.PushService;

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

    public static PutCardResponse jsonToPutCardResponseObject(final String json) {
        try {
            return new ObjectMapper().readValue(json, PutCardResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
