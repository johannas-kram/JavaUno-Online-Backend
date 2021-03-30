package de.markherrmann.javauno.exceptions;

public class InvalidTokenException extends java.lang.IllegalStateException {
    public InvalidTokenException() {
        super("Invalid Token provided.");
    }
}
