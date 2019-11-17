package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.CardType;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LayService {

    private final GameService gameService;
    private final PlayerService playerService;
    private final TurnService turnService;

    @Autowired
    public LayService(GameService gameService, PlayerService playerService, TurnService turnService){
        this.gameService = gameService;
        this.playerService = playerService;
        this.turnService = turnService;
    }

    public String lay(String gameUuid, String playerUuid, Card card){
        Game game = gameService.getGame(gameUuid);
        Player player = playerService.getPlayer(playerUuid, game);
        turnService.failIfInvalidTurnState(game, TurnState.TAKE_DUTIES_OR_CUMULATE, TurnState.LAY_OR_TAKE, TurnState.LAY_TAKEN);
        if(!turnService.isPlayersTurn(game, player)){
            return "failure: it's not your turn";
        }
        failIfInvalidCard(card, player);
        if(!isPlayableCard(game, player, card)){
            return "failure: card does not match";
        }
        layCard(game, player, card);
        return "success";
    }

    private void layCard(Game game, Player player, Card card){
        player.removeCard(card);
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

    private boolean isPlayableCard(Game game, Player player, Card card) {
        if(TurnState.TAKE_DUTIES_OR_CUMULATE.equals(game.getTurnState()) && !isCumulative(game.getTopCard(), card)){
            return false;
        }
        if(TurnState.LAY_TAKEN.equals(game.getTurnState()) && !isLastCard(player, card)){
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

    private boolean isLastCard(Player player, Card card){
        int index = player.getCards().lastIndexOf(card);
        int lastIndex = player.getCardCount()-1;
        return index == lastIndex;
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

    private void failIfInvalidCard(Card card, Player player) throws IllegalArgumentException {
        if(player.getCards().contains(card)){
            throw new IllegalArgumentException("The Player has no such card.");
        }
    }

}
