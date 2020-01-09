package de.markherrmann.javauno.service.push;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.TurnState;
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
        String message = getEnhancedMessage(pushMessage, game);
        String destination = "/api/push/"+game.getUuid();
        pusher.convertAndSend(destination, message);
        lastMessage = pushMessage;
    }

    private String getEnhancedMessage(PushMessage pushMessage, Game game){
        switch(pushMessage){
            case ADDED_PLAYER: return getEnhancedAddedPlayerMessage(pushMessage.getValue(), game);
            case REMOVED_PLAYER: return getEnhancedRemovedPlayerMessage(pushMessage.getValue(), game);
            case PUT_CARD: return getEnhancedPutCardMessage(pushMessage.getValue(), game);
            case DRAWN_CARD: return getEnhancedDrawnCardMessage(pushMessage.getValue(), game);
            case NEXT_TURN: return getEnhancedNextTurnMessage(pushMessage.getValue(), game);
            case SELECTED_COLOR: return getEnhancedSelectColorMessage(pushMessage.getValue(), game);
            default: return pushMessage.getValue();
        }
    }

    private String getEnhancedAddedPlayerMessage(String message, Game game){
        int index = game.getPlayers().size()-1;
        String name = game.getPlayers().get(index).getName();
        return String.format("%s:%d:%s", message, index, name);
    }

    private String getEnhancedRemovedPlayerMessage(String message, Game game){
        int index = game.getToDeleteIndex();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedPutCardMessage(String message, Game game){
        boolean joker = game.getTopCard().isJokerCard();
        if(joker){
            return message+":joker";
        }
        return message;
    }

    private String getEnhancedDrawnCardMessage(String message, Game game){
        boolean countdown = TurnState.FINAL_COUNTDOWN.equals(game.getTurnState());
        if(countdown){
            return message+":countdown";
        }
        return message;
    }

    private String getEnhancedSelectColorMessage(String message, Game game){
        String color = game.getDesiredColor();
        return String.format("%s:%s", message, color);
    }

    private String getEnhancedNextTurnMessage(String message, Game game){
        int index = game.getCurrentPlayerIndex();
        return String.format("%s:%d", message, index);
    }

    public static PushMessage getLastMessage() {
        return lastMessage;
    }
}
