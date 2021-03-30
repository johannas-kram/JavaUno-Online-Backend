package de.markherrmann.javauno.exceptions;

public class FileReadException extends java.lang.IllegalStateException {
    public FileReadException() {
        super("Could not read token file in backend. Please try again later or report this error to me.");
    }
}
