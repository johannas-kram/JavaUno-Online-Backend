package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.UnoState;
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
public class LayServiceGeneralFailTest {

    @Autowired
    private LayService layService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        prepareGame();
    }

    @Test
    public void shouldFailCausedByAnotherTurn(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(card);
        game.setCurrentPlayerIndex(1);

        String result = layService.lay(gameUuid, playerUuid, card);

        assertNotLaid(game, card, result, "failure: it's not your turn");
    }

    @Test
    public void shouldFailCausedByInvalidCard(){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        Card card = game.getTopCard();
        Card wrongCard = findWrongCard(card);
        game.getPlayers().get(0).clearCards();
        game.getPlayers().get(0).addCard(wrongCard);
        Exception exception = new Exception("");
        String result = "";

        try {
            result = layService.lay(gameUuid, playerUuid, card);
        } catch (Exception ex){
            exception = ex;
        }

        assertNotLaid(game, wrongCard, result, "");
        assertException(exception, "IllegalArgumentException", "The Player has no such card.");
    }

    private void assertNotLaid(Game game, Card card, String result, String expextedResult){
        assertThat(result).isEqualTo(expextedResult);
        game.getLayStack().pop();
        assertThat(game.getLayStack()).isEmpty();
        assertThat(game.getPlayers().get(0).getCards()).isNotEmpty();
        assertThat(game.getPlayers().get(0).getCards().get(0)).isEqualTo(card);
        assertThat(game.getTurnState()).isEqualTo(TurnState.LAY_OR_TAKE);
    }

    private void assertException(Exception exception, String exceptionType, String message){
        assertThat(exception.getClass().getSimpleName()).isEqualTo(exceptionType);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    private void prepareGame(){
        String uuid = gameService.createGame();
        game = UnoState.getGames().get(uuid);
        playerService.addPlayer(game.getUuid(), "Max", false);
        playerService.addPlayer(game.getUuid(), "Maria", false);
        playerService.addPlayer(game.getUuid(), "Jana", false);
        playerService.addPlayer(game.getUuid(), "A Name", false);
        gameService.startGame(game.getUuid());
    }

    private Card findWrongCard(Card rightCard){
        Card card = rightCard;
        while(card.equals(rightCard)){
            card = game.getTakeStack().pop();
        }
        return card;
    }

}
