package de.markherrmann.javauno.helper;

import java.security.SecureRandom;
import java.util.Random;

public class UnoRandom {
    private static final long TEST_SEED = 420815;
    public static boolean testModeEnabled = false;

    public static Random getRandom() {
        if(testModeEnabled){
            return new Random(TEST_SEED);
        }
        return new SecureRandom();
    }
}
