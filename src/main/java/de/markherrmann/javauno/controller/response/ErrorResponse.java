package de.markherrmann.javauno.controller.response;

public class ErrorResponse extends GeneralResponse {
    private String error;

    private ErrorResponse(){}

    public ErrorResponse(String error){
        super(false, "error");
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
