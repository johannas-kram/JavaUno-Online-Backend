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
            numberCard = (shuffled.peek().isNumberCard());
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
        for(int value = 0; value <= 9; value++){
            for(Color color : Color.values()){
                pushNumberCard(color, value);
                if(value > 0){
                    pushNumberCard(color, value);
                }
            }
        }
    }

    private static void pushNumberCard(Color color, int value){
        Card numberCard = Card.createNumberCard(color, value);
        cards.push(numberCard);
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
            Card skipCard = Card.createSkipCard(color);
            cards.push(skipCard);
        }
    }

    private static void pushRetourCards(Color color){
        for (int i = 0; i < 2; i++){
            Card retourCard = Card.createRetourCard(color);
            cards.push(retourCard);
        }
    }

    private static void pushTake2Cards(Color color){
        for (int i = 0; i < 2; i++){
            Card take2Card = Card.createTake2Card(color);
            cards.push(take2Card);
        }
    }

    private static void pushJokerCards(){
        for(int i = 0; i < 4; i++){
            Card take4Card = Card.createTake4Card();
            Card jokerCard = Card.createJokerCard();
            cards.push(take4Card);
            cards.push(jokerCard);
        }
    }
}