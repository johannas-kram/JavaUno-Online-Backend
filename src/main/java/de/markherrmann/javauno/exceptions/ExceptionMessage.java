package de.markherrmann.javauno.exceptions;

public enum ExceptionMessage {
    NO_SUCH_GAME("There is no such game."),
    NO_SUCH_PLAYER("There is no such player in this game."),
    NO_SUCH_CARD("The player has no has no such card at this position."),
    NO_SUCH_COLOR("There is no such color in UNO."),
    INVALID_STATE_GAME("The game is in wrong state."),
    INVALID_STATE_TURN("The current turn of the game is in wrong state."),
    NOT_YOUR_TURN("It's not your turn."),
    NOT_ENOUGH_PLAYERS("There are not enough players in the game.");

    private String value;

    ExceptionMessage(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
