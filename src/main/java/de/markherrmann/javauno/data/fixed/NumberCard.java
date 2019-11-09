package de.markherrmann.javauno.data.fixed;

public class NumberCard extends Card {

    public NumberCard(Color color, Number number){
        super(color, number);
    }

    @Override
    public String toString(){
        return super.toString()+":"+getColor()+getNumber().getValue();
    }
}
