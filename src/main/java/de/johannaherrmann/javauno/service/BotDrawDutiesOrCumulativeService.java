package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.data.fixed.Card;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import org.springframework.stereotype.Service;

@Service
public class BotDrawDutiesOrCumulativeService {

    boolean handleDrawDutiesOrCumulative(Game game, Player player) {
        Card topCard = game.getTopCard();
        if(!topCard.isDrawCard()){
            DrawService.drawCards(game, player);
            return false;
        }
        boolean put = putCumulative(game, player, topCard.getDrawValue());
        if(!put){
            DrawService.drawCards(game, player);
        }
        return put;
    }

    private boolean putCumulative(Game game, Player player, int topCardDrawValue){
        for(Card card : player.getCards()){
            if(card.getDrawValue() == topCardDrawValue){
                PutService.putCard(game, player, card, player.getCards().indexOf(card));
                return true;
            }
        }
        return false;
    }
}
