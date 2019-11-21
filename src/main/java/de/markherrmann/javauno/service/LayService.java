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
public class LayService {

    private final GameService gameService;
    private final PlayerService playerService;
    private final TurnService turnService;
    private final HousekeepingService housekeepingService;

    @Autowired
    public LayService(GameService gameService, PlayerService playerService, TurnService turnService, HousekeepingService housekeepingService){
        this.gameService = gameService;
        this.playerService = playerService;
        this.turnService = turnService;
        this.housekeepingService = housekeepingService;
    }

    public String lay(String gameUuid, String playerUuid, String cardString, int cardIndex) throws IllegalArgumentException, IllegalStateException {
        Game game = gameService.getGame(gameUuid);
        synchronized (game){
            Player player = playerService.getPlayer(playerUuid, game);
            Card card = giveCardByString(cardString);
            String preChecksResult = preChecks(game, player, card, cardIndex);
            if(!"ok".equals(preChecksResult)){
                return preChecksResult;
            }
            layCard(game, player, card, cardIndex);
            housekeepingService.updateGameLastAction(game);
        }
        return "success";
    }

    private String preChecks(Game game, Player player, Card card, int cardIndex) throws IllegalStateException {
        if(!gameService.isGameInLifecycle(game, GameLifecycle.RUNNING)){
            return "failure: game is in wrong lifecycle.";
        }
        turnService.failIfInvalidTurnState(
                game,
                TurnState.TAKE_DUTIES_OR_CUMULATE,
                TurnState.LAY_OR_TAKE,
                TurnState.LAY_TAKEN);
        if(!turnService.isPlayersTurn(game, player)){
            return "failure: it's not your turn.";
        }
        failIfInvalidCard(card, player, cardIndex);
        if(!isPlayableCard(game, player, card, cardIndex)){
            return "failure: card does not match.";
        }
        return "ok";
    }

    private void layCard(Game game, Player player, Card card, int cardIndex){
        player.removeCard(cardIndex);
        game.getLayStack().push(card);
        setGameVars(game, card);
        switchTurnState(game);
    }

    private void setGameVars(Game game, Card card){
        setGameTake(game, card);
        setGameSkip(game, card);
        setGameReversed(game, card);
    }

    private void setGameTake(Game game, Card card){
        if(card.isTakeCard()){
            int take = game.getTake() + card.getTake();
            game.setTake(take);
        }
    }

    private void setGameSkip(Game game, Card card){
        if(CardType.SKIP.equals(card.getCardType())){
            game.setSkip(true);
        }
    }

    private void setGameReversed(Game game, Card card){
        if(CardType.RETOUR.equals(card.getCardType())){
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
        if(TurnState.TAKE_DUTIES_OR_CUMULATE.equals(game.getTurnState()) && !isCumulative(game.getTopCard(), card)){
            return false;
        }
        if(TurnState.LAY_TAKEN.equals(game.getTurnState()) && !isLastCard(player, cardIndex)){
            return false;
        }
        if(card.isJokerCard()){
            return true;
        }
        return isMatch(game, card);
    }

    private boolean isCumulative(Card topCard, Card playersCard){
        return topCard.getTake() == playersCard.getTake();
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
            case RETOUR:
            case TAKE2:
                return playersCard.getCardType().equals(topCard.getCardType()) || playersCard.getColor().equals(topCard.getColor());
            case JOKER:
            case TAKE4:
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
