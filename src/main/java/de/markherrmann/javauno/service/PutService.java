package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.CardType;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.exceptions.CardDoesNotMatchException;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import de.markherrmann.javauno.exceptions.IllegalStateException;

import de.markherrmann.javauno.service.push.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PutService {

    private final TurnService turnService;
    private static final Logger LOGGER = LoggerFactory.getLogger(PutService.class);

    @Autowired
    public PutService(TurnService turnService){
        this.turnService = turnService;
    }

    public void put(String gameUuid, String playerUuid, Card card, int cardIndex)
            throws IllegalArgumentException, IllegalStateException, CardDoesNotMatchException {
        Game game = turnService.getGame(gameUuid);
        synchronized (game){
            Player player = turnService.getPlayer(playerUuid, game);
            preChecks(game, player, card, cardIndex);
            if(!isPlayableCard(game, player, card, cardIndex)){
                throw new CardDoesNotMatchException();
            }
            putCard(game, player, card, cardIndex);
            turnService.updateLastAction(game);
            turnService.pushAction(PushMessage.PUT_CARD, game);
            if(player.getCards().isEmpty()){
                turnService.pushAction(PushMessage.FINISHED_GAME, game);
                return;
            }
        }
        if(TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            turnService.finalizeTurn(game);
        }
    }

    private void preChecks(Game game, Player player, Card card, int cardIndex) throws IllegalStateException, IllegalArgumentException {
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

    static void putCard(Game game, Player player, Card card, int cardIndex){
        Card topCard = game.getTopCard();
        player.removeCard(cardIndex);
        game.getDiscardPile().push(card);
        setGameVars(game, card);
        switchTurnState(game);
        LOGGER.info("Put card successfully. Game: {}; Player: {}; playersCard: {}; topCard: {}",
                game.getUuid(),
                player.getUuid(),
                card,
                topCard);
        if(player.getCards().isEmpty()){
            game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
            game.setTurnState(TurnState.FINAL_COUNTDOWN);
        }
    }

    private static void setGameVars(Game game, Card card){
        setGameDraw(game, card);
        setGameSkip(game, card);
        setGameReversed(game, card);
    }

    private static void setGameDraw(Game game, Card card){
        int draw = game.getDrawDuties() + card.getDrawValue();
        game.setDrawDuties(draw);
    }

    private static void setGameSkip(Game game, Card card){
        boolean skipCard = CardType.SKIP.equals(card.getCardType());
        boolean reverseCard = CardType.REVERSE.equals(card.getCardType());
        if(skipCard || (reverseCard && game.getPlayers().size() == 2)){
            game.setSkip(true);
        }
    }

    private static void setGameReversed(Game game, Card card){
        if(CardType.REVERSE.equals(card.getCardType())){
            game.toggleReversed();
        }
    }

    private static void switchTurnState(Game game){
        if(game.getTopCard().isJokerCard()){
            game.setTurnState(TurnState.SELECT_COLOR);
        } else {
            game.setTurnState(TurnState.FINAL_COUNTDOWN);
        }
    }

    private boolean isPlayableCard(Game game, Player player, Card card, int cardIndex) {
        if(TurnState.DRAW_DUTIES_OR_CUMULATIVE.equals(game.getTurnState()) && !isCumulative(game.getTopCard(), card)){
            logNotMatchingCard(game, player, card, "It's not cumulative");
            return false;
        }
        if(TurnState.PUT_DRAWN.equals(game.getTurnState()) && !isLastCard(player, cardIndex)){
            logNotMatchingCard(game, player, card, "It's not the drawn card");
            return false;
        }
        boolean match = isMatch(game, card);
        if(!match){
            logNotMatchingCard(game, player, card, "The cards do not match");
        }
        return match;
    }

    private boolean isCumulative(Card topCard, Card playersCard){
        return topCard.getDrawValue() == playersCard.getDrawValue();
    }

    private boolean isLastCard(Player player, int index){
        return index == player.getCards().size()-1;
    }

    static boolean isMatch(Game game, Card playersCard){
        if(playersCard.isJokerCard()){
            return true;
        }
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
            logInvalidCard(player, card);
            throw new IllegalArgumentException(message);
        }
        Card foundCard = player.getCards().get(cardIndex);
        if(!foundCard.equals(card)){
            logInvalidCard(player, card);
            throw new IllegalArgumentException(message);
        }
    }

    private void logNotMatchingCard(Game game, Player player, Card playersCard, String reason){
        Card topCard = game.getTopCard();
        LOGGER.warn(
                "card does not match. {}. Game: {}; Player: {}; playersCard: {}; topCard: {}",
                reason,
                game.getUuid(),
                player.getUuid(),
                playersCard,
                topCard);
    }

    private void logInvalidCard(Player player, Card card){
        LOGGER.warn(
                "The Player has no such card at this position. Cards: {}; CardToPut: {}",
                player.getCards(),
                card);
    }

}
