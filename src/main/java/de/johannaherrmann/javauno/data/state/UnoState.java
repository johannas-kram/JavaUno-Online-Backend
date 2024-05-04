package de.johannaherrmann.javauno.data.state;

import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.exceptions.ExceptionMessage;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnoState {
	private static Map<String, Game> games = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(UnoState.class);

	public static synchronized Game getGame(String uuid) throws IllegalArgumentException {
        if(!games.containsKey(uuid)){
            LOGGER.error("There is no such game. UUID: {}", uuid);
            throw new IllegalArgumentException(ExceptionMessage.NO_SUCH_GAME.getValue());
        }
        return games.get(uuid);
    }

	public static synchronized void putGame(Game game){
	    games.put(game.getUuid(), game);
    }

    public static synchronized void removeGame(String gameUuid){
	    games.remove(gameUuid);
        LOGGER.info("Removed old game: {}", gameUuid);
    }

    public static synchronized Set<Map.Entry<String, Game>> getGamesEntrySet(){
	    return games.entrySet();
    }

    public static synchronized boolean containsGame(String uuid){
	    return games.containsKey(uuid);
    }

    public static void clear(){
	    games.clear();
    }
}
