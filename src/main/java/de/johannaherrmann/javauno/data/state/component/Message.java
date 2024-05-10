package de.johannaherrmann.javauno.data.state.component;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String content;
    private final String playerPublicUuid;
    private final long time;

    public Message(String content, String playerPublicUuid, long time) {
        this.content = content;
        this.playerPublicUuid = playerPublicUuid;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public String getPlayerPublicUuid() {
        return playerPublicUuid;
    }

    public long getTime() {
        return time;
    }
}
