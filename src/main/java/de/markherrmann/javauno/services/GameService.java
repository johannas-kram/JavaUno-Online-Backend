package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.Player;
import org.springframework.stereotype.Service;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.List;
import java.util.Stack;

@Service
public class GameService {

    public String createGame(){
        Game game = new Game();
        UnoState.getGames().put(game.getUuid(), game);
        return game.getUuid();
    }

    public String addPlayer(String gameUuid, String name, boolean bot) throws IllegalArgumentException, InvalidStateException {
        Game game = getGame(gameUuid);
        if(!game.playersAddable()){
            throw new InvalidStateException("Game is started. Players can not be added anymore.");
        }
        Player player = new Player(name, bot);
        game.getPlayer().put(player.getUuid(), player);
        game.getPlayerList().add(player);
        return game.getUuid();
    }

    public void startGame(String gameUuid) throws IllegalArgumentException, IllegalStateException {
        Game game = getGame(gameUuid);
        if(!game.gameStartable()){
            throw new InvalidStateException("Current round is not finished. New round can not be started yet.");
        }
        resetGame(game);
        Stack<Card> deck = Deck.getShuffled();
        game.getLayStack().push(deck.pop());
        giveCards(game.getPlayerList(), deck);
        game.getTakeStack().addAll(deck);
    }

    public List<Card> getOwnCards(String gameUuid, String playerUuid) throws IllegalArgumentException {
        Game game = getGame(gameUuid);
        Player player = getPlayer(playerUuid, game);
        return player.getCards();
    }

    private void resetGame(Game game){
        for(Player player : game.getPlayerList()){
            player.getCards().clear();
        }
        game.getTakeStack().clear();
        game.getLayStack().clear();
        game.setDesiredColor(null);
        game.setTake(0);
        if(game.isReversed()){
            game.toggleReversed();
        }
    }

    private void giveCards(List<Player> playerList, Stack<Card> deck){
        for(int cards = 0; cards < 7; cards++){
            for(Player player : playerList){
                player.getCards().add(deck.pop());
            }
        }
    }

    private Game getGame(String gameUuid) throws IllegalArgumentException {
        if(!UnoState.getGames().containsKey(gameUuid)){
            throw new IllegalArgumentException("There is no game with uuid " +gameUuid);
        }
        return UnoState.getGames().get(gameUuid);
    }

    private Player getPlayer(String playerUuid, Game game) throws IllegalArgumentException {
        if(!game.getPlayer().containsKey(playerUuid)){
            throw new IllegalArgumentException("There is no player with uuid " +playerUuid + " in game with uuid " + game.getUuid());
        }
        return game.getPlayer().get(playerUuid);
    }
}
