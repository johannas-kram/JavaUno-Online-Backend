package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.ErrorResponse;
import de.markherrmann.javauno.controller.response.GeneralResponse;
import de.markherrmann.javauno.exceptions.IllegalArgumentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

class ErrorResponseUtil {
    static ResponseEntity<GeneralResponse> getExceptionResponseEntity(Exception exception){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if(exception instanceof RuntimeException) {
            status = exception instanceof IllegalArgumentException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        }
        ErrorResponse response = new ErrorResponse(exception);
        return ResponseEntity.status(status).body(response);
    }

    static HttpStatus getErrorStatus(HttpServletRequest request){
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = 500;
        if(status != null) {
            statusCode = Integer.valueOf(status.toString());
        }
        return HttpStatus.resolve(statusCode);
    }

    static ResponseEntity<ErrorResponse> getErrorResponseEntity(HttpStatus httpStatus){
        String error;
        switch (httpStatus){
            case NOT_FOUND:
                error = "Bad URL. Wrong path or missing url arguments";
                break;
            case METHOD_NOT_ALLOWED:
                error = "Wrong request method";
                break;
            case UNSUPPORTED_MEDIA_TYPE:
                error = "Unsupported media type";
                break;
            default:
                error = "Unknown error";
                break;
        }
        ErrorResponse response = new ErrorResponse(error);
        return ResponseEntity.status(httpStatus).body(response);
    }
}
