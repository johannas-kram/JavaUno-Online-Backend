package de.johannaherrmann.javauno.controller.response;

public class SetPlayerResponse extends GeneralResponse {

    private String playerUuid;

    private SetPlayerResponse(){}

    public SetPlayerResponse(boolean success, String message, String playerUuid) {
        super(success, message);
        this.playerUuid = playerUuid;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }
}
