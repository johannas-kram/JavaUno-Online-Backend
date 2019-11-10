package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HousekeepingService {

    static final long MAX_DURATION_WITHOUT_ACTION = 30*60*1000; // 30 minutes

    void updateGameLastAction(Game game){
        game.setLastAction(System.currentTimeMillis());
    }

    void removeOldGames(){
        Map<String, Game> games = UnoState.getGames();
        List<String> uuidsToRemove = new ArrayList<>();
        for(Map.Entry<String, Game> gameEntry : games.entrySet()){
            long lastAction = gameEntry.getValue().getLastAction();
            long now = System.currentTimeMillis();
            if((now - lastAction) > MAX_DURATION_WITHOUT_ACTION){
                uuidsToRemove.add(gameEntry.getValue().getUuid());
            }
        }
        for(String uuidToRemove : uuidsToRemove){
            games.remove(uuidToRemove);
        }
    }

    void removeGame(String uuid){
        UnoState.removeGame(uuid);
    }

}
