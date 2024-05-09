package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.data.state.UnoState;
import de.johannaherrmann.javauno.data.state.component.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Objects;

@Service
public class PersistenceService {


    private final String gamesPath;

    @Autowired
    public PersistenceService(Environment environment){
        this.gamesPath = environment.getProperty("games.path") + "/";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveGame (Game game) throws IOException {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteGame (String gameUuid) {
        new File(gamesPath + gameUuid).delete();
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
