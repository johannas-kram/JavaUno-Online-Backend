package de.markherrmann.javauno.data.fixed;

import java.util.Collections;
import java.util.Stack;

public class Deck {

    private static Stack<Card> cards;

    static {
        initStack();
    }

    public static Stack<Card> getShuffled(){
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
            pushReverseCards(color);
            pushSkipCards(color);
            pushDraw2Cards(color);
        }
    }

    private static void pushSkipCards(Color color){
        for (int i = 0; i < 2; i++){
            Card skipCard = Card.createSkipCard(color);
            cards.push(skipCard);
        }
    }

    private static void pushReverseCards(Color color){
        for (int i = 0; i < 2; i++){
            Card reverseCard = Card.createReverseCard(color);
            cards.push(reverseCard);
        }
    }

    private static void pushDraw2Cards(Color color){
        for (int i = 0; i < 2; i++){
            Card draw2Card = Card.createDraw2Card(color);
            cards.push(draw2Card);
        }
    }

    private static void pushJokerCards(){
        for(int i = 0; i < 4; i++){
            Card draw4Card = Card.createDraw4Card();
            Card jokerCard = Card.createJokerCard();
            cards.push(draw4Card);
            cards.push(jokerCard);
        }
    }
}
