package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.TestHelper;
import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import de.johannaherrmann.javauno.data.state.component.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersistenceServiceTest {
    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    private final String gamesPath = "./data/games/";

    @Before
    public void setup () {
        UnoState.clear();
    }

    @After
    public void teardown () {
        UnoState.clear();
    }

    @Test
    public void shouldSaveGameCorrectly () throws Exception {
        Game game = TestHelper.prepareAndStartGame(gameService, playerService);
        game.setBotifyPlayerByRequestThread(new Thread(() -> dummyBotifyPlayerByRequestThread(game, game.getPlayers().get(0))));

        persistenceService.saveGame(game);

        assertThat(new File(gamesPath + game.getUuid())).exists();
        Game deserializedGame = deserializeGame(gamesPath + game.getUuid());
        assertThat(deserializedGame.getUuid()).isEqualTo(game.getUuid());
        assertThat(deserializedGame.getPlayers().get(0).getUuid()).isEqualTo(game.getPlayers().get(0).getUuid());
        assertThat(deserializedGame.getPlayers().get(0).getCards().get(0).getUuid()).isEqualTo(game.getPlayers().get(0).getCards().get(0).getUuid());
        assertThat(deserializedGame.getBotifyPlayerByRequestThread()).isNull();
    }

    @Test
    public void shouldDeleteGameCorrectly () {

    }

    @Test
    public void shouldLoadGamesCorrectly () {

    }

    private void dummyBotifyPlayerByRequestThread(Game ignoredGame, Player ignoredPlayer) {}

    private Game deserializeGame (String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream
                = new FileInputStream(path);
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        Game game = (Game) objectInputStream.readObject();
        objectInputStream.close();
        return game;
    }
}
