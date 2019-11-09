package de.markherrmann.javauno.data.state;

import de.markherrmann.javauno.data.state.components.Game;

import java.util.HashMap;
import java.util.Map;

public class UnoState {
	private static Map<String, Game> games = new HashMap<>();
	
	public static Map<String, Game> getGames(){
		return games;
	}
}