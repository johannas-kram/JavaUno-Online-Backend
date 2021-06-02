package de.markherrmann.javauno.service.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
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

    public void pushDirectly(String pushUuid, String... parts){
        StringBuilder sb = new StringBuilder();
        for(String part : parts){
            if(sb.toString().length() > 0){
                sb.append(":");
            }
            sb.append(part);
        }
        String message = sb.toString();
        String destination = "/api/push/"+pushUuid;
        pusher.convertAndSend(destination, message);
        lastMessage = PushMessage.valueOf(parts[0].replace("-", "_").toUpperCase());
    }

    public void push(PushMessage pushMessage, Game game){
        String message = getEnhancedMessage(pushMessage, game);
        String destination = "/api/push/"+game.getUuid();
        pusher.convertAndSend(destination, message);
        lastMessage = pushMessage;
    }

    private String getEnhancedMessage(PushMessage pushMessage, Game game){
        switch(pushMessage){
            case STARTED_GAME: return getEnhancedStartedGameMessage(pushMessage.getValue(), game);
            case ADDED_PLAYER: return getEnhancedAddedPlayerMessage(pushMessage.getValue(), game);
            case REMOVED_PLAYER: return getEnhancedRemovedPlayerMessage(pushMessage.getValue(), game);
            case BOTIFIED_PLAYER: return getEnhancedBotifiedPlayerMessage(pushMessage.getValue(), game);
            case REQUEST_BOTIFY_PLAYER: return getEnhancedRequestBotifyPlayerMessage(pushMessage.getValue(), game);
            case CANCEL_BOTIFY_PLAYER: return getEnhancedCancelBotifyPlayerMessage(pushMessage.getValue(), game);
            case PUT_CARD: return getEnhancedPutCardMessage(pushMessage.getValue(), game);
            case DRAWN_CARD: return getEnhancedDrawnCardMessage(pushMessage.getValue(), game);
            case DRAWN_CARDS: return getEnhancedDrawnCardsMessage(pushMessage.getValue(), game);
            case NEXT_TURN: return getEnhancedNextTurnMessage(pushMessage.getValue(), game);
            case SELECTED_COLOR: return getEnhancedSelectColorMessage(pushMessage.getValue(), game);
            case SAID_UNO: return getEnhancedSaidUnoMessage(pushMessage.getValue(), game);
            case FINISHED_GAME: return getEnhancedFinishedGameMessage(pushMessage.getValue(), game);
            case REQUEST_STOP_PARTY: return getEnhancedRequestStopPartyMessage(pushMessage.getValue(), game);
            case REVOKE_REQUEST_STOP_PARTY: return getEnhancedRevokeRequestStopPartyMessage(pushMessage.getValue(), game);
            case STOP_PARTY: return getEnhancedStopPartyMessage(pushMessage.getValue(), game);
            default: return pushMessage.getValue();
        }
    }

    private String getEnhancedStartedGameMessage(String message, Game game){
        int index = game.getCurrentPlayerIndex();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedAddedPlayerMessage(String message, Game game){
        int index = game.getPlayers().size()-1;
        String name = game.getPlayers().get(index).getName();
        return String.format("%s:%d:%s", message, index, name);
    }

    private String getEnhancedRemovedPlayerMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedBotifiedPlayerMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedRequestBotifyPlayerMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        Player player = game.getPlayers().get(index);
        return String.format("%s:%s", message, player.getUuid());
    }

    private String getEnhancedCancelBotifyPlayerMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        Player player = game.getPlayers().get(index);
        return String.format("%s:%s", message, player.getPublicUuid());
    }

    private String getEnhancedPutCardMessage(String message, Game game){
        return message + ":" + asJsonString(game.getTopCard());
    }

    private String getEnhancedDrawnCardMessage(String message, Game game){
        boolean countdown = TurnState.FINAL_COUNTDOWN.equals(game.getTurnState());
        if(countdown){
            return message+":countdown";
        }
        return message;
    }

    private String getEnhancedDrawnCardsMessage(String message, Game game){
        return String.format("%s:%d:%s", message, game.getDrawnCards(), game.getDrawReason());
    }

    private String getEnhancedSelectColorMessage(String message, Game game){
        String color = game.getDesiredColor();
        return String.format("%s:%s", message, color);
    }

    private String getEnhancedSaidUnoMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        int cardCount = game.getPlayers().get(index).getCardCount();
        return String.format("%s:%d", message, cardCount);
    }

    private String getEnhancedNextTurnMessage(String message, Game game){
        int index = game.getCurrentPlayerIndex();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedFinishedGameMessage(String message, Game game){
        int party = game.getParty();
        return String.format("%s:%d", message, party);
    }

    private String getEnhancedRequestStopPartyMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedRevokeRequestStopPartyMessage(String message, Game game){
        int index = game.getPlayerIndexForPush();
        return String.format("%s:%d", message, index);
    }

    private String getEnhancedStopPartyMessage(String message, Game game){
        int party = game.getParty();
        return String.format("%s:%d", message, party);
    }

    public static PushMessage getLastMessage() {
        return lastMessage;
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
