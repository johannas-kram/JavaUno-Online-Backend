package de.markherrmann.javauno.data.fixed;


import com.fasterxml.jackson.annotation.JsonIgnore;

public final class Card {

    private CardType cardType;
    private Color color;
    private Number number;
    private int value;
    private boolean numberCard;
    private boolean jokerCard;
    private boolean takeCard;
    private int take;

	public Card(){

    }

    public Card(CardType cardType, Color color, Number number){
        this.cardType = cardType;
	    this.color = color;
        this.number = number;
    }

    public Color getColor() {
        return color;
    }

    @JsonIgnore
    public Number getNumber() {
        return number;
    }

    public int getValue() {
	    if(number == null){
	        return -1;
        }
        return number.getValue();
    }

    public CardType getCardType(){
        return cardType;
    }

    public boolean isNumberCard() {
        return CardType.NUMBER.equals(cardType);
    }

    public boolean isJokerCard() {
        return CardType.JOKER.equals(cardType) || CardType.TAKE4.equals(cardType);
    }

    public boolean isTakeCard() {
        return CardType.TAKE2.equals(cardType) || CardType.TAKE4.equals(cardType);
    }

    public int getTake() {
        if(CardType.TAKE2.equals(cardType)){
            return 2;
        }
        if(CardType.TAKE4.equals(cardType)){
            return 4;
        }
        return 0;
    }

    @Override
    public String toString(){
	    String str = cardType.toString();
	    if(color != null){
	        str += ":" + color;
        }
        if(getValue() != -1){
            str += ":" + getValue();
        }
	    return str;
    }
}
