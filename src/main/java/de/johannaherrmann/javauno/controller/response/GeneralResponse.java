package de.johannaherrmann.javauno.controller.response;

public class GeneralResponse {
    private boolean success;
    private String message;

    protected GeneralResponse(){}

    public GeneralResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    protected void setSuccess(boolean success) {
        this.success = success;
    }

    protected void setMessage(String message) {
        this.message = message;
    }
}
