package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Color;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.helper.UnoRandom;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    private final BotDrawDutiesOrCumulativeService botDrawDutiesOrCumulativeService;
    private final BotMaybePutService botMaybePutService;
    private final BotFindColorService botSelectColorService;
    private final PushService pushService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BotService.class);

    private static int lastSayUnoRandomNumber = -1;

    @Autowired
    public BotService(BotDrawDutiesOrCumulativeService botDrawDutiesOrCumulativeService,
                      BotMaybePutService botMaybePutService, BotFindColorService botSelectColorService,
                      PushService pushService) {
        this.botDrawDutiesOrCumulativeService = botDrawDutiesOrCumulativeService;
        this.botMaybePutService = botMaybePutService;
        this.botSelectColorService = botSelectColorService;
        this.pushService = pushService;
    }

    void makeTurn(Game game, Player player){
        int party = game.getParty();
        if(GameLifecycle.RUNNING.equals(game.getGameLifecycle())){
            doSleep(1200);
        }
        while(!TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())
                && GameLifecycle.RUNNING.equals(game.getGameLifecycle()) && game.getParty() == party){
            handleTurnState(game, player);
            if(player.getCards().isEmpty()){
                game.setLastWinner(game.getCurrentPlayerIndex());
                pushService.push(PushMessage.FINISHED_GAME, game);
                LOGGER.info("Successfully finished party. Game: {}; party: {}; winner: {}", game.getUuid(), game.getParty(), player.getUuid());
            }
        }
        if(!player.getCards().isEmpty()){
            boolean saidUno = maybeSayUno(game, player);
            doSleep(saidUno ? 2500 : 3000);
        }

    }

    private void handleTurnState(Game game, Player player){
        switch(game.getTurnState()){
            case DRAW_PENALTIES: handleDrawPenalties(game, player); break;
            case DRAW_DUTIES_OR_CUMULATIVE: handleDrawDutiesOrCumulative(game, player); break;
            case PUT_OR_DRAW: handlePutOrDraw(game, player); break;
            case PUT_DRAWN: handlePutDrawn(game, player); break;
            case SELECT_COLOR: handleSelectColor(game, player); break;
        }
    }


    private void handleDrawPenalties(Game game, Player player) {
        DrawService.drawCards(game, player);
        pushService.push(PushMessage.DRAWN_CARDS, game);
        doSleep(game.getDrawDuties() > 0 ? 1000 : 2000);
    }


    private void handleDrawDutiesOrCumulative(Game game, Player player) {
        boolean put = botDrawDutiesOrCumulativeService.handleDrawDutiesOrCumulative(game, player);
        pushService.push(put ? PushMessage.PUT_CARD : PushMessage.DRAWN_CARDS, game);
        if(!TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            doSleep(1000);
        }
    }

    private void handlePutOrDraw(Game game, Player player) {
        boolean put = botMaybePutService.maybePut(game, player, false);
        if(!put){
            DrawService.drawCard(game, player);
        }
        pushService.push(put ? PushMessage.PUT_CARD : PushMessage.DRAWN_CARD, game);
        if(!TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            doSleep(1000);
        }
    }


    private void handlePutDrawn(Game game, Player player) {
        boolean put = botMaybePutService.maybePut(game, player, true);
        pushService.push(put ? PushMessage.PUT_CARD : PushMessage.KEPT_CARD, game);
        if(!put){
            KeepService.keep(game, player);
        }
        if(put && game.getTopCard().isJokerCard()){
            doSleep(1000);
        }
    }


    private void handleSelectColor(Game game, Player player) {
        Color color = botSelectColorService.findColor(player.getCards());
        SelectColorService.selectColor(game, color.name());
        pushService.push(PushMessage.SELECTED_COLOR, game);
    }

    private boolean maybeSayUno(Game game, Player player) {
        if(!GameLifecycle.RUNNING.equals(game.getGameLifecycle())){
            return false;
        }
        if(player.getCards().size() == 1){
            int sayUnoRandomNumber = UnoRandom.getRandom().nextInt(10);
            lastSayUnoRandomNumber = sayUnoRandomNumber;
            if(sayUnoRandomNumber < 9){
                doSleep(1000);
                if(!GameLifecycle.RUNNING.equals(game.getGameLifecycle())){
                    return false;
                }
                SayUnoService.sayUno(game, player);
                pushService.push(PushMessage.SAID_UNO, game);
                return true;
            }
        }
        return false;
    }

    static int getLastSayUnoRandomNumber(){
        return lastSayUnoRandomNumber;
    }

    private void doSleep(int durance){
        try {
            Thread.sleep(durance);
        } catch (InterruptedException ex){
            LOGGER.error("ERROR! Bot sleeping Interrupted. while loop with bad performance will be used.", ex);
            waitWithWhileLoop(durance);
        }
    }

    private void waitWithWhileLoop(int durance){
        long start = System.currentTimeMillis();
        long diff;
        do {
            diff = System.currentTimeMillis() - start;
        } while(diff < durance);
    }
}
