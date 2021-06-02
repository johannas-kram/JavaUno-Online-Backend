package de.markherrmann.javauno.controller.response;

import de.markherrmann.javauno.data.fixed.Card;

public class PutCardResponse extends GeneralResponse {
    private Card card;

    private PutCardResponse(){}

    public PutCardResponse(Card card) {
        super(true, "success");
        this.card = card;
    }

    public PutCardResponse(Exception ex) {
        super(false, "failure: " + ex);
    }

    public Card getCard() {
        return card;
    }
}
