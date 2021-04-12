package de.markherrmann.javauno.service.push;

import de.markherrmann.javauno.TestHelper;
import de.markherrmann.javauno.data.state.component.Game;
import de.markherrmann.javauno.data.state.component.GameLifecycle;
import de.markherrmann.javauno.WsTestUtils;
import de.markherrmann.javauno.service.GameService;
import de.markherrmann.javauno.service.PlayerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import de.markherrmann.javauno.WsTestUtils.MyStompFrameHandler;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import de.markherrmann.javauno.WsTestUtils.MyStompSessionHandler;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PushServiceTest {

    @Value("${local.server.port}")
    private int port;


    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private WsTestUtils wsTestUtils = new WsTestUtils();

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    PushService pushService;

    private Game game;

    @Before
    public void setUp() throws Exception {
        String wsUrl = "ws://localhost:" + port + "/api/ws";
        stompClient = wsTestUtils.createWebSocketClient();
        stompSession = stompClient.connect(wsUrl, new MyStompSessionHandler()).get();
        game = TestHelper.prepareAndStartGame(gameService, playerService);
        game.setGameLifecycle(GameLifecycle.SET_PLAYERS);
    }

    @After
    public void tearDown() throws Exception {
        stompSession.disconnect();
        stompClient.stop();
    }

    @Test
    public void shouldConnectToSocket() throws Exception {
        assertThat(stompSession.isConnected()).isTrue();
    }

    @Test
    public void shouldPushTestMessage() throws Exception {
        PushMessage testMessage = PushMessage.SAID_UNO;
        CompletableFuture<String> resultKeeper = prepare();

        pushService.push(testMessage, game);

        assertThat(resultKeeper.get(2, SECONDS)).isEqualTo(testMessage.getValue()+":7");
        assertThat(PushService.getLastMessage()).isEqualTo(testMessage);
    }

    private CompletableFuture<String> prepare() throws Exception {
        CompletableFuture<String> resultKeeper = new CompletableFuture<>();
        stompSession.subscribe(
                "/api/push/" + game.getUuid(),
                new MyStompFrameHandler(resultKeeper::complete));
        Thread.sleep(1000);
        return resultKeeper;
    }
}

