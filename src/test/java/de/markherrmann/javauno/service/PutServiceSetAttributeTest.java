package de.markherrmann.javauno.service;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.fixed.Card;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.TurnState;
import de.markherrmann.javauno.service.push.PushMessage;
import de.markherrmann.javauno.service.push.PushService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PutServiceSetAttributeTest {

    @Autowired
    private PutService putService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private Game game;

    @Before
    public void setup(){
        game = TestHelper.prepareAndStartGame(gameService, playerService);
        game.getDiscardPile().clear();
    }

    @Test
    public void  shouldSetSkip(){
        Card topCard = TestHelper.giveCardByString("NUMBER:BLUE:1");
        Card playersCard = TestHelper.giveCardByString("SKIP:BLUE");

        putCard(topCard, playersCard);

        assertThat(game.isSkip()).isTrue();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    @Test
    public void  shouldSetReverseAsSkip(){
        game.getPlayers().remove(2);
        game.getPlayers().remove(2);
        Card topCard = TestHelper.giveCardByString("NUMBER:BLUE:1");
        Card playersCard = TestHelper.giveCardByString("REVERSE:BLUE");

        putCard(topCard, playersCard);

        assertThat(game.isSkip()).isTrue();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    @Test
    public void  shouldSetReverse(){
        Card topCard = TestHelper.giveCardByString("NUMBER:BLUE:1");
        Card playersCard = TestHelper.giveCardByString("REVERSE:BLUE");

        putCard(topCard, playersCard);

        assertThat(game.isSkip()).isFalse();
        assertThat(game.isReversed()).isTrue();
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    @Test
    public void  shouldSetDrawDuties2(){
        Card topCard = TestHelper.giveCardByString("NUMBER:BLUE:1");
        Card playersCard = TestHelper.giveCardByString("DRAW_2:BLUE");

        putCard(topCard, playersCard);

        assertThat(game.getDrawDuties()).isEqualTo(2);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    @Test
    public void  shouldSetDrawDuties2Cumulative(){
        game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
        game.setDrawDuties(2);
        Card topCard = TestHelper.giveCardByString("DRAW_2:GREEN");
        Card playersCard = TestHelper.giveCardByString("DRAW_2:BLUE");

        putCard(topCard, playersCard);

        assertThat(game.getDrawDuties()).isEqualTo(4);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    @Test
    public void  shouldSetDrawDuties4(){
        Card topCard = TestHelper.giveCardByString("NUMBER:BLUE:1");
        Card playersCard = TestHelper.giveCardByString("DRAW_4");

        putCard(topCard, playersCard);

        assertThat(game.getDrawDuties()).isEqualTo(4);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);

    }

    @Test
    public void  shouldSetDrawDuties4Cumulative(){
        game.setTurnState(TurnState.DRAW_DUTIES_OR_CUMULATIVE);
        game.setDrawDuties(4);
        Card topCard = TestHelper.giveCardByString("DRAW_4");
        Card playersCard = TestHelper.giveCardByString("DRAW_4");

        putCard(topCard, playersCard);

        assertThat(game.getDrawDuties()).isEqualTo(8);
        assertThat(PushService.getLastMessage()).isEqualTo(PushMessage.PUT_CARD);
    }

    private void putCard(Card topCard, Card playersCard){
        String gameUuid = game.getUuid();
        String playerUuid = game.getPlayers().get(0).getUuid();
        game.getDiscardPile().push(topCard);
        game.getPlayers().get(0).getCards().add(0, playersCard);

        putService.put(gameUuid, playerUuid, playersCard, 0);
    }


}
