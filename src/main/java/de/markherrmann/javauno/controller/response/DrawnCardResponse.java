package de.markherrmann.javauno.controller.response;

import de.markherrmann.javauno.data.fixed.Card;

public class DrawnCardResponse {
    private boolean success;
    private String message;
    private Card card;
    private boolean match;

    private DrawnCardResponse(){}

    public DrawnCardResponse(Card card, boolean match) {
        this.success = true;
        this.message = "success";
        this.card = card;
        this.match = match;
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

    public boolean isMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }
}
