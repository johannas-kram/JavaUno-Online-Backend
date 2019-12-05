package de.markherrmann.javauno.controller.response;

import de.markherrmann.javauno.data.fixed.Card;

public class DrawnCardResponse extends GeneralResponse {
    private Card card;
    private boolean match;

    private DrawnCardResponse(){}

    public DrawnCardResponse(Card card, boolean match) {
        super(true, "success");
        this.card = card;
        this.match = match;
    }

    public DrawnCardResponse(Exception ex) {
        super(false, "failure: " + ex);
    }

    public Card getCard() {
        return card;
    }

    public boolean isMatch() {
        return match;
    }
}
