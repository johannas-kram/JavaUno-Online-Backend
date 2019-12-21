package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.component.Game;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HousekeepingService {

    static final long MAX_DURATION_WITHOUT_ACTION = 30*60*1000; // 30 minutes

    void updateLastAction(Game game){
        game.setLastAction(System.currentTimeMillis());
    }

    void removeOldGames(){
        List<String> uuidsToRemove = new ArrayList<>();
        for(Map.Entry<String, Game> gameEntry : UnoState.getGamesEntrySet()){
            long lastAction = gameEntry.getValue().getLastAction();
            long now = System.currentTimeMillis();
            if((now - lastAction) > MAX_DURATION_WITHOUT_ACTION){
                uuidsToRemove.add(gameEntry.getValue().getUuid());
            }
        }
        for(String uuidToRemove : uuidsToRemove){
            UnoState.removeGame(uuidToRemove);
        }
    }

    boolean removeGameIfNoHumans(Game game){
        if(game.getHumans().isEmpty()){
            UnoState.removeGame(game.getUuid());
            return true;
        }
        return false;
    }

}
