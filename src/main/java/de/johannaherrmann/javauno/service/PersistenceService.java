package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import jakarta.annotation.PostConstruct;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveGame (Game game) {
        String path = gamesPath + game.getUuid();
        new File(gamesPath).mkdirs();
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            objectOutputStream.writeObject(game);
            objectOutputStream.flush();
            LOGGER.info("Successfully saved game with uuid {}", game.getUuid());
        } catch (IOException exception) {
            LOGGER.error("Could not save game with uuid {}. Error: {}", game.getUuid(), exception.getMessage());
        }
    }

    public void deleteGame (String gameUuid) {
        boolean deleted = new File(gamesPath + gameUuid).delete();
        if (deleted) {
            LOGGER.info("Successfully deleted game with uuid {}", gameUuid);
        } else {
            LOGGER.error("Could not delete game with uuid {}", gameUuid);
        }
    }

    @PostConstruct
    public void loadGames () throws IOException, ClassNotFoundException {
        File[] games = new File(gamesPath).listFiles();
        if (games == null || games.length == 0) {
            LOGGER.info("No games found to load.");
            return;
        }
        for (File gameFile : games) {
            FileInputStream fileInputStream
                    = new FileInputStream(gameFile.getPath());
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            Game game = (Game) objectInputStream.readObject();
            objectInputStream.close();
            game.setLastAction(System.currentTimeMillis());
            UnoState.putGame(game);
            LOGGER.info("Successfully loaded game with uuid {}", game.getUuid());
        }
    }
}
