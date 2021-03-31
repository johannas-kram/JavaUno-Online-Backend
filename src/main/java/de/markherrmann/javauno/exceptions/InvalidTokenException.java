package de.markherrmann.javauno.exceptions;

public class InvalidTokenException extends java.lang.SecurityException {
    public InvalidTokenException() {
        super(ExceptionMessage.INVALID_TOKEN.getValue());
    }
}
