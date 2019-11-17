package de.markherrmann.javauno.data.fixed;

public final class Card {

    private CardType cardType;
    private String color;
    private int value;
    private boolean numberCard;
    private boolean jokerCard;
    private boolean takeCard;
    private int take;

    private Card(){}

    private Card(CardType cardType, Color color, int value){
        this.cardType = cardType;
	    this.color = color.name();
        this.value = value;
    }

    private Card(CardType cardType, int value){
        this.cardType = cardType;
        this.value = value;
        this.color = "joker";
    }

    static Card createNumberCard(Color color, int value){
        value = Math.abs(value) % 10;
	    return new Card(CardType.NUMBER, color, value);
    }

    static Card createSkipCard(Color color){
        return new Card(CardType.SKIP, color, 20);
    }

    static Card createRetourCard(Color color){
        return new Card(CardType.RETOUR, color, 20);
    }

    static Card createTake2Card(Color color){
        return new Card(CardType.TAKE2, color, 20);
    }

    static Card createTake4Card(){
        return new Card(CardType.TAKE4, 50);
    }

    static Card createJokerCard(){
        return new Card(CardType.JOKER, 50);
    }

    public String getColor() {
        return color;
    }

    public CardType getCardType(){
        return cardType;
    }

    public int getValue() {
        return value;
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
	    if(!isJokerCard()){
	        str += ":" + color;
        }
        if(isNumberCard()){
            str += ":" + value;
        }
	    return str;
    }

    @Override
    public boolean equals(Object o){
        return this.toString().equals(o.toString());
    }
}
