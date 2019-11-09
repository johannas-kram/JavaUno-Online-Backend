package de.markherrmann.javauno.services;

import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.fixed.Deck;
import de.markherrmann.javauno.data.state.UnoState;
import de.markherrmann.javauno.data.state.components.Game;
import de.markherrmann.javauno.data.state.components.GameLifecycle;
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
        if(!isGameInLivecycle(game, GameLifecycle.ADD_PLAYERS)){
            throw new InvalidStateException("Game is started. Players can not be added anymore.");
        }
        Player player = new Player(name, bot);
        game.getPlayer().put(player.getUuid(), player);
        game.getPlayerList().add(player);
        return game.getUuid();
    }

    public void startGame(String gameUuid) throws IllegalArgumentException, IllegalStateException {
        Game game = getGame(gameUuid);
        if(isGameInLivecycle(game, GameLifecycle.RUNNING)){
            throw new InvalidStateException("Current round is not finished. New round can not be started yet.");
        }
        resetGame(game);
        Stack<Card> deck = Deck.getShuffled();
        game.getLayStack().push(deck.pop());
        giveCards(game.getPlayerList(), deck);
        game.getTakeStack().addAll(deck);
        game.setGameLifecycle(GameLifecycle.RUNNING);
    }

    private void resetGame(Game game){
        for(Player player : game.getPlayerList()){
            player.getCards().clear();
        }
        game.getTakeStack().clear();
        game.getLayStack().clear();
        game.setDesiredColor(null);
        resetPlayers(game);
        if(game.isReversed()){
            game.toggleReversed();
        }
    }

    private void resetPlayers(Game game){
        for(Player player : game.getPlayerList()){
            player.setTake(0);
            player.setUnoSaid(false);
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

    private boolean isGameInLivecycle(Game game, GameLifecycle gameLifecycle){
        return game.getGameLifecycle().equals(gameLifecycle);
    }
}
