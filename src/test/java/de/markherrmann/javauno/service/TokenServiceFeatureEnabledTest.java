package de.markherrmann.javauno.service;

import de.markherrmann.javauno.exceptions.FileReadException;
import de.markherrmann.javauno.exceptions.InvalidTokenException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = "feature.tokenized_game_create=on")
public class TokenServiceFeatureEnabledTest {

    private static final String TEST_HASH = "$2a$10$IYdL0ox.MC5.eRF0kVYaD.1kXHGb2inPvnimTSpR5.hAFLtoIX4SK";
    private static final String TEST_TOKEN_FILE_NAME = "Dsnmn7Twqd1";
    private static final String TEST_TOKEN_MESSAGE = "j8yZ15Ji210";
    private static final String TEST_TOKEN = TEST_TOKEN_FILE_NAME+"."+TEST_TOKEN_MESSAGE;
    private static final String REGEX_MATCHING_INVALID_TOKEN = TEST_TOKEN_MESSAGE+"."+TEST_TOKEN_FILE_NAME;

    @Autowired
    TokenService tokenService;

    @Before
    public void setUp() throws IOException {
        File tokensDir = new File("./tokens");
        if (!tokensDir.exists()){
            tokensDir.mkdirs();
        }
        File testTokenFile = new File("./tokens/"+TEST_TOKEN_FILE_NAME);
        testTokenFile.createNewFile();
        Files.write(testTokenFile.toPath(), TEST_HASH.getBytes());
    }

    @After
    public void tearDown(){
        new File("./tokens/"+TEST_TOKEN_FILE_NAME).setReadable(true, true);
        new File("./tokens/"+TEST_TOKEN_FILE_NAME).delete();
        new File("./tokens").delete();
    }

    @Test
    public void shouldValidateTokenValid(){
        Exception exception = null;

        try {
            tokenService.checkForTokenizedGameCreate(TEST_TOKEN);
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNull();
    }

    @Test
    public void shouldValidateTokenInvalid_RegexMatch(){
        Exception exception = null;

        try {
            tokenService.checkForTokenizedGameCreate(REGEX_MATCHING_INVALID_TOKEN);
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void shouldValidateTokenInvalid_RegexDisMatch(){
        Exception exception = null;

        try {
            tokenService.checkForTokenizedGameCreate("invalid");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void shouldValidateTokenInvalid_Empty(){
        Exception exception = null;

        try {
            tokenService.checkForTokenizedGameCreate("");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void shouldFailValidateTokenCausedByIOException(){
        Exception exception = null;
        new File("./tokens/"+TEST_TOKEN_FILE_NAME).setReadable(false, true);

        try {
            tokenService.checkForTokenizedGameCreate(TEST_TOKEN);
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNotNull();
        assertThat(exception).isInstanceOf(FileReadException.class);
    }

}
