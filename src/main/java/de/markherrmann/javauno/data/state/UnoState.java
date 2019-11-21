package de.markherrmann.javauno.data.state;

import de.markherrmann.javauno.data.state.component.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnoState {
	private static Map<String, Game> games = new HashMap<>();

	public static synchronized Game getGame(String uuid) throws IllegalArgumentException {
        if(!games.containsKey(uuid)){
            throw new IllegalArgumentException("There is no such game.");
        }
        return games.get(uuid);
    }

	public static synchronized void putGame(Game game){
	    games.put(game.getUuid(), game);
    }

    public static synchronized void removeGame(String gameUuid){
	    games.remove(gameUuid);
    }

    public static synchronized Set<Map.Entry<String, Game>> getGamesEntrySet(){
	    return games.entrySet();
    }

    public static synchronized boolean containsGame(String uuid){
	    return games.containsKey(uuid);
    }
}
