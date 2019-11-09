package de.markherrmann.javauno.data.fixed;


public abstract class Card {
	
	private Color color;
	private Number number;

	public Card(){}

    public Card(Color color, Number number){
        this.color = color;
        this.number = number;
    }

    public Card(Color color){
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Number getNumber() {
        return number;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }

    public String getType(){
        return this.getClass().getSimpleName();
    }
}