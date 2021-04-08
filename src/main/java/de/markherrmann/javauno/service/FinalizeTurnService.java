package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinalizeTurnService {

    private Game game;
    private static final Logger LOGGER = LoggerFactory.getLogger(TurnService.class);

    private final PushService pushService;

    private final BotService botService;

    @Autowired
    public FinalizeTurnService(PushService pushService, BotService botService) {
        this.pushService = pushService;
        this.botService = botService;
    }

    void finalize(Game game){
        synchronized (game){
            finalizeTurn(game);
        }
        pushService.push(PushMessage.NEXT_TURN, game);
        Player player = game.getPlayers().get(game.getCurrentPlayerIndex());
        LOGGER.info("Successfully terminated turn. Game: {}; CurrentPlayerIndex: {}", game.getUuid(), game.getCurrentPlayerIndex());
        handleBotTurn(game, player);
    }

    void handleBotTurn(Game game, Player player){
        if(player.isBot() && GameLifecycle.RUNNING.equals(game.getGameLifecycle())){
            botService.makeTurn(game, player);
            if(player.getCardCount() > 0 && GameLifecycle.RUNNING.equals(game.getGameLifecycle())){
                finalize(game);
            }
        }
    }

    void finalizeTurn(Game game){
        this.game = game;
        setPlayersDrawPenalties();
        setNextPlayer();
    }

    private void setPlayersDrawPenalties(){
        Player player = game.getPlayers().get(game.getCurrentPlayerIndex());
        boolean lastCard = player.getCards().size() == 1;
        boolean saidUno = player.isUnoSaid();
        if((lastCard && !saidUno) || (!lastCard && saidUno)){
            player.setDrawPenalties(2);
        }
    }

    private void setNextPlayer(){
        int index = getNextPlayerIndex();
        TurnState turnState = getNextTurnState(index);
        game.setCurrentPlayerIndex(index);
        game.setTurnState(turnState);
    }

    private int getNextPlayerIndex(){
        int currentIndex = game.getCurrentPlayerIndex();
        int players = game.getPlayers().size();
        int steps = 1;
        if(game.isSkip()){
            game.setSkip(false);
            steps = 2;
        }
        if(game.isReversed()){
            steps = players - steps;
        }
        return (currentIndex + steps) % players;
    }

    private TurnState getNextTurnState(int index){
        Player player = game.getPlayers().get(index);
        if(player.getDrawPenalties() > 0){
            return TurnState.DRAW_PENALTIES;
        }
        if(game.getDrawDuties() > 0){
            return TurnState.DRAW_DUTIES_OR_CUMULATIVE;
        }
        return TurnState.PUT_OR_DRAW;
    }
}
