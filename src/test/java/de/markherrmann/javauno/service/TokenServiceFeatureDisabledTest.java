package de.markherrmann.javauno.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenServiceFeatureDisabledTest {

    @Autowired
    TokenService tokenService;

    @Test
    public void shouldNotValidateToken(){
        Exception exception = null;

        try {
            tokenService.checkForTokenizedGameCreate("");
        } catch(Exception ex){
            exception = ex;
        }

        assertThat(exception).isNull();
    }

}
