package de.johannaherrmann.javauno.service;

import de.johannaherrmann.javauno.data.state.component.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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

    public void deleteGame (String gameUuid) {

    }

    public void loadGames () {

    }
}
