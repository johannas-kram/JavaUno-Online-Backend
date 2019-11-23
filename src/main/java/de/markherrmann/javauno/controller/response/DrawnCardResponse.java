package de.markherrmann.javauno.controller.response;

import de.markherrmann.javauno.data.fixed.Card;

public class DrawnCardResponse {
    private boolean success;
    private String message;
    private Card card;

    public DrawnCardResponse(Card card) {
        this.success = true;
        this.message = "success";
        this.card = card;
    }

    public DrawnCardResponse(Exception ex) {
        this.success = false;
        this.message = "failure: " + ex;
    }


    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Card getCard() {
        return card;
    }
}
