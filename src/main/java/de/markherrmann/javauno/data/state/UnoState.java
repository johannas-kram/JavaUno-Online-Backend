package de.markherrmann.javauno.data.state;

import de.markherrmann.javauno.data.state.component.Game;

import java.util.HashMap;
import java.util.Map;

public class UnoState {
	private static Map<String, Game> games = new HashMap<>();
	
	public static synchronized Map<String, Game> getGames(){
		return games;
	}

	public static synchronized void putGame(Game game){
	    games.put(game.getUuid(), game);
    }

    public static synchronized void removeGame(String gameUuid){
	    games.remove(gameUuid);
    }
}
