package de.johannaherrmann.javauno.controller.response;

public class GameCreateResponse extends GeneralResponse {
    private String gameUuid;

    private GameCreateResponse(){}

    public GameCreateResponse(String gameUuid){
        super(true, "success");
        this.gameUuid = gameUuid;
    }

    public String getGameUuid() {
        return gameUuid;
    }
}
