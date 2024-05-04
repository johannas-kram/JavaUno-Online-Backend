package de.johannaherrmann.javauno.controller.response;

public class ErrorResponse extends GeneralResponse {
    private ErrorResponse(){}

    public ErrorResponse(Exception exception){
        super(false, "failure: " + exception);
    }

    public ErrorResponse(String error){
        super(false, "failure: " + error);
    }
}
