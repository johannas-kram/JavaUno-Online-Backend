package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Color;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BotFindColorService {

    Color findColor(List<Card> playersCards){
        Map<Color, Integer> counts = getClearCounts();
        for(Card card : playersCards){
            if(!card.isJokerCard()){
                countColor(card, counts);
            }
        }
        return selectBest(counts);
    }

    private void countColor(Card card, Map<Color, Integer> counts){
        Color color = Color.valueOf(card.getColor());
        int count = counts.get(color)+1;
        counts.put(color, count);
    }

    private Color selectBest(Map<Color, Integer> counts){
        int max = 0;
        Color color = Color.RED;
        for(Map.Entry<Color, Integer> countEntry: counts.entrySet()){
            int value = countEntry.getValue();
            if(value > max){
                max = value;
                color = countEntry.getKey();
            }
        }
        return color;
    }

    private Map<Color, Integer> getClearCounts(){
        Map<Color, Integer> counts = new HashMap<>();
        counts.put(Color.RED, 0);
        counts.put(Color.GREEN, 0);
        counts.put(Color.BLUE, 0);
        counts.put(Color.YELLOW, 0);
        return counts;
    }
}
