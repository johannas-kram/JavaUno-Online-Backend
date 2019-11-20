package de.markherrmann.javauno.controller.response;

public class SetPlayerResponse {

    private boolean success;
    private String message;
    private String playerUuid;

    public SetPlayerResponse(){

    }

    public SetPlayerResponse(boolean success, String message, String playerUuid) {
        this.success = success;
        this.message = message;
        this.playerUuid = playerUuid;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }
}
