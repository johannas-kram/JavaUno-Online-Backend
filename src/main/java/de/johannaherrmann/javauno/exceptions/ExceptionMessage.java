package de.johannaherrmann.javauno.exceptions;

public enum ExceptionMessage {
    NO_SUCH_GAME("There is no such game."),
    NO_SUCH_PLAYER("There is no such player in this game."),
    NO_SUCH_CARD("The player has no has no such card at this position."),
    NO_SUCH_COLOR("There is no such color in UNO."),
    INVALID_STATE_GAME("The game is in wrong state."),
    INVALID_STATE_TURN("The current turn of the game is in wrong state."),
    NOT_YOUR_TURN("It's not your turn."),
    NOT_ENOUGH_PLAYERS("There are not enough players in the game."),
    PLAYERS_LIMIT_REACHED("There are already 10 players in the game. Players limit is reached."),
    INVALID_TOKEN("Invalid Token provided."),
    FILE_READ_ERROR("Could not read token file in backend. Please try again later or report this error to me."),
    EMPTY_CHAT_MESSAGE("Chat message is empty. Will not save empty messages.")
    ;

    private String value;

    ExceptionMessage(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
