package de.markherrmann.javauno.data.fixed;

import java.util.Collections;
import java.util.Stack;

public class Deck {

    private static Stack<Card> cards;

    static {
        initStack();
    }

    public static synchronized Stack<Card> getShuffled(){
        Stack<Card> shuffled;
        boolean numberCard;
        do {
            shuffled = new Stack<>();
            shuffled.addAll(cards);
            Collections.shuffle(shuffled);
            numberCard = (shuffled.peek() instanceof NumberCard);
        } while(!numberCard);
        return shuffled;
    }

    private static void initStack(){
        cards = new Stack<>();
        pushNumberCards();
        pushActionCards();
        pushJokerCards();
    }


    private static void pushNumberCards(){
        for(Number number : Number.values()){
            for(Color color : Color.values()){
                pushNumberCard(color, number);
                if(number.getValue() > 0){
                    pushNumberCard(color, number);
                }
            }
        }
    }

    private static void pushNumberCard(Color color, Number number){
        cards.push(new NumberCard(color, number));
    }

    private static void pushActionCards(){
        for(Color color : Color.values()) {
            pushRetourCards(color);
            pushSkipCards(color);
            pushTake2Cards(color);
        }
    }

    private static void pushSkipCards(Color color){
        for (int i = 0; i < 2; i++){
            cards.push(new SkipCard(color));
        }
    }

    private static void pushRetourCards(Color color){
        for (int i = 0; i < 2; i++){
            cards.push(new RetourCard(color));
        }
    }

    private static void pushTake2Cards(Color color){
        for (int i = 0; i < 2; i++){
            cards.push(new Take2Card(color));
        }
    }

    private static void pushJokerCards(){
        for(int i = 0; i < 4; i++){
            cards.push(new JokerCard());
            cards.push(new Take4Card());
        }
    }
}