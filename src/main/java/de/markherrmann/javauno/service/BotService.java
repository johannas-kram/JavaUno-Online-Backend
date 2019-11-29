package de.markherrmann.javauno.service;

import de.markherrmann.javauno.data.fixed.Color;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.Player;
import de.markherrmann.javauno.data.state.component.TurnState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BotService {

    private final BotDrawDutiesOrCumulativeService botDrawDutiesOrCumulativeService;
    private final BotMaybePutService botMaybePutService;
    private final BotFindColorService botSelectColorService;
    private final Logger logger = LoggerFactory.getLogger(BotService.class);

    @Autowired
    public BotService(BotDrawDutiesOrCumulativeService botDrawDutiesOrCumulativeService,
                      BotMaybePutService botMaybePutService, BotFindColorService botSelectColorService) {
        this.botDrawDutiesOrCumulativeService = botDrawDutiesOrCumulativeService;
        this.botMaybePutService = botMaybePutService;
        this.botSelectColorService = botSelectColorService;
    }

    void makeTurn(Game game, Player player){
        if(TurnState.PUT_OR_DRAW.equals(game.getTurnState())){
            doSleep(2000);
        }
        while(!TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            handleTurnState(game, player);
        }
        boolean saidUno = maybeSayUno(game, player);
        doSleep(saidUno ? 2500 : 3000);
    }

    private void handleTurnState(Game game, Player player){
        switch(game.getTurnState()){
            case DRAW_PENALTIES: handleDrawPenalties(game, player); break;
            case DRAW_DUTIES_OR_CUMULATIVE: handleDrawDutiesOrCumulative(game, player); break;
            case DRAW_DUTIES: handleDrawDuties(game, player); break;
            case PUT_OR_DRAW: handlePutOrDraw(game, player); break;
            case PUT_DRAWN: handlePutDrawn(game, player); break;
            case SELECT_COLOR: handleSelectColor(game, player); break;
        }
    }


    private void handleDrawPenalties(Game game, Player player) {
        DrawService.drawCard(game, player);
        int drawPenalties = player.getDrawPenalties();
        int sleepDurance = drawPenalties > 0 ? 500: 2000;
        doSleep(sleepDurance);
    }


    private void handleDrawDutiesOrCumulative(Game game, Player player) {
        botDrawDutiesOrCumulativeService.handleDrawDutiesOrCumulative(game, player);
        if(!TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            doSleep(500);
        }
    }


    private void handleDrawDuties(Game game, Player player) {
        DrawService.drawCard(game, player);
        int drawDuties = game.getDrawDuties();
        int sleepDurance = drawDuties > 0 ? 500: 2000;
        doSleep(sleepDurance);
    }


    private void handlePutOrDraw(Game game, Player player) {
        boolean put = botMaybePutService.maybePut(game, player, false);
        if(!put){
            DrawService.drawCard(game, player);
        }
        if(!TurnState.FINAL_COUNTDOWN.equals(game.getTurnState())){
            doSleep(500);
        }
    }


    private void handlePutDrawn(Game game, Player player) {
        boolean put = botMaybePutService.maybePut(game, player, true);
        if(!put){
            RemainService.remain(game, player);
        }
    }


    private void handleSelectColor(Game game, Player player) {
        Color color = botSelectColorService.findColor(player.getCards());
        SelectColorService.selectColor(game, color.name());
    }

    private boolean maybeSayUno(Game game, Player player) {
        Random random = new Random();
        if(player.getCards().size() == 1){
            if(random.nextInt(10) < 8){
                doSleep(500);
                SayUnoService.sayUno(game, player);
                return true;
            }
        }
        return false;
    }

    private void doSleep(int durance){
        try {
            Thread.sleep(durance);
        } catch (InterruptedException ex){
            logger.error("ERROR! Bot sleeping Interrupted. while loop with bad performance will be used.", ex);
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
