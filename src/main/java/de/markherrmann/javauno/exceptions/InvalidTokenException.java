package de.markherrmann.javauno.exceptions;

public class InvalidTokenException extends java.lang.IllegalStateException {
    public InvalidTokenException() {
        super(ExceptionMessage.INVALID_TOKEN.getValue());
    }
}
