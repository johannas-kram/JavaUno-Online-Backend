package de.markherrmann.javauno.exceptions;

public class FileReadException extends java.lang.IllegalStateException {
    public FileReadException() {
        super(ExceptionMessage.FILE_READ_ERROR.getValue());
    }
}
