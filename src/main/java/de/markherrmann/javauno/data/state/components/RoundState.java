package de.markherrmann.javauno.data.state.components;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Color;

import java.util.List;

public class RoundState {
    private List<Byte> playerCardsCount;
    private List<Card> ownCards;
    private Card topCard;
    private Color desiredColor;
    private boolean ownTurn;

    public RoundState(List<Byte> playerCardsCount, List<Card> ownCards, Card topCard, Color desiredColor, boolean ownTurn) {
        this.playerCardsCount = playerCardsCount;
        this.ownCards = ownCards;
        this.topCard = topCard;
        this.desiredColor = desiredColor;
        this.ownTurn = ownTurn;
    }

    public List<Byte> getPlayerCardsCount() {
        return playerCardsCount;
    }

    public List<Card> getOwnCards() {
        return ownCards;
    }

    public Card getTopCard() {
        return topCard;
    }

    public Color getDesiredColor() {
        return desiredColor;
    }

    public boolean isOwnTurn() {
        return ownTurn;
    }
}
