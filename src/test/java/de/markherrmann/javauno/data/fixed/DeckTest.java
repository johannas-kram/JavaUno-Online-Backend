package de.markherrmann.javauno.data.fixed;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeckTest {

    @Test
    public void shouldGetShuffled(){
        List<String> names = getAllNames();

        Stack<Card> shuffledDeck = Deck.getShuffled();

        Assertions.assertThat(shuffledDeck).isNotEmpty();
        assertThat(shuffledDeck.size()).isEqualTo(108);
        assertContainsAll(shuffledDeck, names);
    }

    private void assertContainsAll(List<Card> test, List<String> control){
        List<String> testNames = new ArrayList<>();
        for(Card card : test){
            assertThat(control).contains(card.toString());
            assertThat(control).contains(card.toString());
            testNames.add(card.toString());
        }
        for(String name : control){
            assertThat(testNames).contains(name);
        }
    }

    private List<String> getAllNames(){
        List<String> names = new ArrayList<>();
        addNamesNormal(names);
        addNamesJoker(names);
        addNamesAction(names);
        return names;
    }

    private void addNamesNormal(List<String> names){
        for(int value = 0; value <= 9; value++){
            for(Color color : Color.values()){
                names.add("NUMBER:"+color+":"+value);
                if(value > 0){
                    names.add("NUMBER:"+color+":"+value);
                }
            }
        }
    }

    private void addNamesJoker(List<String> names){
        for(int i = 0; i < 4; i++){
            names.add("JOKER");
            names.add("DRAW_4");
        }
    }

    private void addNamesAction(List<String> names){
        for(Color color : Color.values()){
            addNamesSkip(names, color);
            addNamesReverse(names, color);
            addNamesDraw(names, color);
        }
    }

    private void addNamesSkip(List<String> names, Color color){
        names.add("SKIP:"+color);
        names.add("SKIP:"+color);
    }

    private void addNamesReverse(List<String> names, Color color){
        names.add("REVERSE:"+color);
        names.add("REVERSE:"+color);
    }

    private void addNamesDraw(List<String> names, Color color){
        names.add("DRAW_2:"+color);
        names.add("DRAW_2:"+color);
    }
}
