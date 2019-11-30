package de.markherrmann.javauno.service.push;

public enum PushMessage {

    STARTED_GAME("started-game"),
    ADDED_PLAYER("added-player"),
    REMOVED_PLAYER("removed-player"),
    PUT_CARD("put-card"),
    DRAWN_CARD("drawn-card"),
    SELECTED_COLOR("selected-color"),
    SAID_UNO("said-uno"),
    NEXT_TURN("next-turn"),
    FINISHED_GAME("finished-game"),
    END("end");

    private final String value;

    private PushMessage(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
