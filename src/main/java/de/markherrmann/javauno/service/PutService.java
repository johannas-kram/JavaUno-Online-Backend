package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.CardType;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
public class PutService {

    private final TurnService turnService;

    @Autowired
    public PutService(TurnService turnService){
        this.turnService = turnService;
    }

    public String put(String gameUuid, String playerUuid, String cardString, int cardIndex) throws IllegalArgumentException, IllegalStateException {
        Game game = turnService.getGame(gameUuid);
        synchronized (game){
            Player player = turnService.getPlayer(playerUuid, game);
            Card card = giveCardByString(cardString);
            preChecks(game, player, card, cardIndex);
            if(!isPlayableCard(game, player, card, cardIndex)){
                return "failure: card does not match.";
            }
            putCard(game, player, card, cardIndex);
            turnService.updateLastAction(game);
        }
        return "success";
    }

    private void preChecks(Game game, Player player, Card card, int cardIndex) throws IllegalStateException {
        if(!turnService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            throw new IllegalStateException("game is in wrong lifecycle.");
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.DRAW_DUTIES_OR_CUMULATIVE,
                TurnState.PUT_OR_DRAW,
                TurnState.PUT_DRAWN);
        if(!turnService.isPlayersTurn(game, player)){
            throw new IllegalStateException("it's not your turn.");
        }
        failIfInvalidCard(card, player, cardIndex);
    }

    private void putCard(Game game, Player player, Card card, int cardIndex){
        player.removeCard(cardIndex);
        game.getDiscardPile().push(card);
        setGameVars(game, card);
        switchTurnState(game);
    }

    private void setGameVars(Game game, Card card){
        setGameDraw(game, card);
        setGameSkip(game, card);
        setGameReversed(game, card);
    }

    private void setGameDraw(Game game, Card card){
        int draw = game.getDrawDuties() + card.getDrawValue();
        game.setDrawDuties(draw);
    }

    private void setGameSkip(Game game, Card card){
        if(CardType.SKIP.equals(card.getCardType())){
            game.setSkip(true);
        }
    }

    private void setGameReversed(Game game, Card card){
        if(CardType.REVERSE.equals(card.getCardType())){
            game.toggleReversed();
        }
    }

    private void switchTurnState(Game game){
        if(game.getTopCard().isJokerCard()){
            game.setTurnState(TurnState.SELECT_COLOR);
        } else {
            game.setTurnState(TurnState.FINAL_COUNTDOWN);
        }
    }

    private boolean isPlayableCard(Game game, Player player, Card card, int cardIndex) {
        if(TurnState.DRAW_DUTIES_OR_CUMULATIVE.equals(game.getTurnState()) && !isCumulative(game.getTopCard(), card)){
            return false;
        }
        if(TurnState.PUT_DRAWN.equals(game.getTurnState()) && !isLastCard(player, cardIndex)){
            return false;
        }
        if(card.isJokerCard()){
            return true;
        }
        return isMatch(game, card);
    }

    private boolean isCumulative(Card topCard, Card playersCard){
        return topCard.getDrawValue() == playersCard.getDrawValue();
    }

    private boolean isLastCard(Player player, int index){
        return index == player.getCards().size()-1;
    }

    private boolean isMatch(Game game, Card playersCard){
        Card topCard = game.getTopCard();
        switch(topCard.getCardType()){
            case NUMBER:
                return playersCard.getValue() == topCard.getValue() || playersCard.getColor().equals(topCard.getColor());
            case SKIP:
            case REVERSE:
            case DRAW_2:
                return playersCard.getCardType().equals(topCard.getCardType()) || playersCard.getColor().equals(topCard.getColor());
            case JOKER:
            case DRAW_4:
                return playersCard.getColor().equals(game.getDesiredColor());
        }
        return false;
    }

    private void failIfInvalidCard(Card card, Player player, int cardIndex) throws IllegalArgumentException {
        String message = "The Player has no such card at this position.";
        if(player.getCards().size() < cardIndex+1){
            throw new IllegalArgumentException(message);
        }
        Card foundCard = player.getCards().get(cardIndex);
        if(!foundCard.equals(card)){
            throw new IllegalArgumentException(message);
        }
    }

    private Card giveCardByString(String cardString) throws IllegalArgumentException {
        Stack<Card> cards = Deck.getShuffled();
        Card foundCard = null;
        for(Card card : cards){
            if(card.toString().equals(cardString)){
                foundCard = card;
                break;
            }
        }
        if(foundCard == null){
            throw new IllegalArgumentException("The Player has no such card at this position.");
        }
        return foundCard;
    }

}
