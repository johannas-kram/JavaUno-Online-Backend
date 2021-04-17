package de.markherrmann.javauno.data.state.component;

public class Message {

    private final String content;
    private final String playerPublicUuid;

    public Message(String content, String playerPublicUuid) {
        this.content = content;
        this.playerPublicUuid = playerPublicUuid;
    }

    public String getContent() {
        return content;
    }

    public String getPlayerPublicUuid() {
        return playerPublicUuid;
    }
}
