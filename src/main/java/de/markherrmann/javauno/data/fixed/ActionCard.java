package de.markherrmann.javauno.data.fixed;

public abstract class ActionCard extends Card {
    public ActionCard(Color color){
        super(color);
    }

    @Override
    public String toString(){
        return super.toString()+":"+getColor();
    }
}
