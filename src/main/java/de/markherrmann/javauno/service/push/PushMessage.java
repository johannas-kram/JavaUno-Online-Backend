package de.markherrmann.javauno.service.push;

public enum PushMessage {

    STARTED_GAME("started-game"),
    ADDED_PLAYER("added-player"),
    REMOVED_PLAYER("removed-player"),
    BOTIFIED_PLAYER("botified-player"),
    PUT_CARD("put-card"),
    DRAWN_CARD("drawn-card"),
    KEPT_CARD("kept-card"),
    SELECTED_COLOR("selected-color"),
    SAID_UNO("said-uno"),
    NEXT_TURN("next-turn"),
    FINISHED_GAME("finished-game"),
    REQUEST_STOP_PARTY("request-stop-party"),
    REVOKE_REQUEST_STOP_PARTY("revoke-request-stop-party"),
    STOP_PARTY("stop-party"),
    END("end"),
    SWITCH_IN("switch-in"),
    SWITCH_FINISHED("switch-finished")
    ;

    private final String value;

    PushMessage(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
