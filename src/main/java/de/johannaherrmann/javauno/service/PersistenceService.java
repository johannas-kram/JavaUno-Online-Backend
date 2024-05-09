package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;

@Service
public class PersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceService.class);

    private final String gamesPath;

    @Autowired
    public PersistenceService(Environment environment){
        this.gamesPath = environment.getProperty("games.path") + "/";
    }

    public void saveGame (Game game) {
        String path = gamesPath + game.getUuid();
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(game);
            objectOutputStream.flush();
        } catch (IOException exception) {
            LOGGER.error("Could not save game with uuid {}", game.getUuid(), exception);
        }
    }

    public void deleteGame (String gameUuid) {
        boolean deleted = new File(gamesPath + gameUuid).delete();
        if (!deleted) {
            LOGGER.error("Could not delete game with uuid {}", gameUuid);
        }
    }

    public void loadGames () throws IOException, ClassNotFoundException {
        File[] games = new File(gamesPath).listFiles();
        for (File gameFile : Objects.requireNonNull(games)) {
            FileInputStream fileInputStream
                    = new FileInputStream(gameFile.getPath());
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            Game game = (Game) objectInputStream.readObject();
            objectInputStream.close();
            game.setLastAction(System.currentTimeMillis());
            UnoState.putGame(game);
        }
    }
}
