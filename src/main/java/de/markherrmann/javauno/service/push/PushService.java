package de.markherrmann.javauno.service.push;

import de.markherrmann.javauno.data.state.component.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PushService {

    private final SimpMessagingTemplate pusher;
    private static PushMessage lastMessage;

    @Autowired
    public PushService(SimpMessagingTemplate pusher) {
        this.pusher = pusher;
    }

    public void push(PushMessage pushMessage, Game game){
        String destination = "/api/push/"+game.getUuid();
        pusher.convertAndSend(destination, pushMessage.getValue());
        lastMessage = pushMessage;
    }

    public static PushMessage getLastMessage() {
        return lastMessage;
    }
}
