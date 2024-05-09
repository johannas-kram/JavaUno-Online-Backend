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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

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
        deleteGames();
    }

    @After
    public void teardown () {
        UnoState.clear();
        deleteGames();
    }

    @Test
    public void shouldSaveGameCorrectly () throws Exception {
        Game game = TestHelper.prepareAndStartGame(gameService, playerService);
        game.setBotifyPlayerByRequestThread(new Thread(() -> dummyBotifyPlayerByRequestThread(game, game.getPlayers().get(0))));

        persistenceService.saveGame(game);

        assertThat(new File(gamesPath + game.getUuid())).exists();
        Game deserializedGame = deserializeGame(gamesPath + game.getUuid());
        assertGamesEqual(deserializedGame, game);
        assertThat(deserializedGame.getBotifyPlayerByRequestThread()).isNull();
    }

    @Test
    public void shouldDeleteGameCorrectly () {

    }

    @Test
    public void shouldLoadGamesCorrectly () throws Exception {
        Game game1 = TestHelper.prepareAndStartGame(gameService, playerService);
        Game game2 = TestHelper.prepareAndStartGame(gameService, playerService);
        serializeGame(game1);
        serializeGame(game2);
        Game loadedGame1 = null;
        Game loadedGame2 = null;
        Exception exception = null;
        UnoState.clear();

        try {
            persistenceService.loadGames();
            loadedGame1 = UnoState.getGame(game1.getUuid());
            loadedGame2 = UnoState.getGame(game2.getUuid());
        } catch (Exception ex) {
            exception = ex;
        }

        assertThat(exception).isNull();
        assertThat(loadedGame1).isNotNull();
        assertThat(loadedGame2).isNotNull();
        assertGamesEqual(loadedGame1, game1);
        assertGamesEqual(loadedGame2, game2);
    }

    private void assertGamesEqual (Game game1, Game game2) {
        assertThat(game1.getUuid()).isEqualTo(game2.getUuid());
        assertThat(game1.getPlayers().get(0).getUuid()).isEqualTo(game2.getPlayers().get(0).getUuid());
        assertThat(game1.getPlayers().get(0).getCards().get(0).getUuid()).isEqualTo(game2.getPlayers().get(0).getCards().get(0).getUuid());
    }

    private void dummyBotifyPlayerByRequestThread(Game ignoredGame, Player ignoredPlayer) {}

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void serializeGame (Game game) throws IOException {
        String path = gamesPath + game.getUuid();
        new File(gamesPath).mkdirs();
        FileOutputStream fileOutputStream
                = new FileOutputStream(path);
        ObjectOutputStream objectOutputStream
                = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(game);
        objectOutputStream.flush();
        objectOutputStream.close();
    }

    private Game deserializeGame (String path) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream
                = new FileInputStream(path);
        ObjectInputStream objectInputStream
                = new ObjectInputStream(fileInputStream);
        Game game = (Game) objectInputStream.readObject();
        objectInputStream.close();
        return game;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteGames() {
        File[] games = new File(gamesPath).listFiles();
        for (File game : Objects.requireNonNull(games)) {
            game.delete();
        }
    }
}
