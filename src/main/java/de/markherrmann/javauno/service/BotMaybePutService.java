package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BotMaybePutService {

    boolean maybePut(Game game, Player player, boolean putDrawn){
        if(putDrawn){
            return maybePutDrawn(game, player);
        }
        List<Card> matches = getMatches(game, player.getCards());
        if(matches.isEmpty()){
            return false;
        }
        Card card = chooseRandomly(matches);
        PutService.putCard(game, player, card, player.getCards().indexOf(card));
        return true;
    }

    private boolean maybePutDrawn(Game game, Player player){
        Card drawnCard = player.getCards().get(player.getCardCount()-1);
        boolean matches = PutService.isMatch(game, drawnCard);
        if(matches){
            PutService.putCard(game, player, drawnCard, player.getCards().indexOf(drawnCard));
        }
        return matches;
    }

    private List<Card> getMatches(Game game, List<Card> playersCards){
        List<Card> matches = new ArrayList<>();
        for(Card card : playersCards){
            if(PutService.isMatch(game, card)){
                matches.add(card);
            }
        }
        return matches;
    }

    private Card chooseRandomly(List<Card> matches){
        Random random = new Random();
        int randomIndex = random.nextInt(matches.size());
        return matches.get(randomIndex);
    }
}
