package de.markherrmann.javauno.service;

import de.markherrmann.javauno.exceptions.FileReadException;
import de.markherrmann.javauno.exceptions.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class TokenService {

    private final boolean featureEnabled;

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    public TokenService(Environment environment){
        this.featureEnabled = "on".equals(environment.getProperty("feature.tokenized_game_create"));
    }

    public void checkForTokenizedGameCreate(String token) {
        if(!featureEnabled){
            return;
        }
        if(!isValidToken(token)){
            throw new InvalidTokenException();
        }
    }

    private boolean isValidToken(String token) {
        String tokenRegex = "^([a-zA-Z0-9_-]{11})\\.([a-zA-Z0-9_-]{11})$";
        if(!token.matches(tokenRegex)){
            LOGGER.error("Token {} does not match the token syntax.", token);
            return false;
        }
        String fileName = token.replaceFirst(tokenRegex, "$1");
        String message = token.replaceFirst(tokenRegex, "$2");
        File tokenFile = new File("./tokens/"+fileName);
        if(tokenFile.exists()){
            String hash = readHashFromFile(tokenFile);
            boolean valid =  isMessageMatchingHash(message, hash);
            if(!valid){
                LOGGER.error("Token {} does not match with hash in file {}.", token, fileName);
            }
            return valid;
        }
        LOGGER.error("There is no token file {} for token {}.", fileName, token);
        return false;
    }

    private String readHashFromFile(File tokenFile){
        try {
            return new String(Files.readAllBytes(tokenFile.toPath()));
        } catch(IOException exception){
            throw new FileReadException();
        }
    }

    private boolean isMessageMatchingHash(String message, String hash){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(message, hash);
    }

}
