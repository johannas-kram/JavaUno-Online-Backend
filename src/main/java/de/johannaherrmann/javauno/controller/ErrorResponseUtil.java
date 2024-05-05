package de.johannaherrmann.javauno.controller;

import de.johannaherrmann.javauno.controller.response.ErrorResponse;
import de.johannaherrmann.javauno.controller.response.GeneralResponse;
import de.johannaherrmann.javauno.exceptions.FileReadException;
import de.johannaherrmann.javauno.exceptions.IllegalArgumentException;
import de.johannaherrmann.javauno.exceptions.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

class ErrorResponseUtil {
    static ResponseEntity<GeneralResponse> getExceptionResponseEntity(Exception exception){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if(exception instanceof RuntimeException) {
            status = exception instanceof IllegalArgumentException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        }
        if(exception instanceof InvalidTokenException) {
            status = HttpStatus.UNAUTHORIZED;
        }
        if(exception instanceof FileReadException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorResponse response = new ErrorResponse(exception);
        return ResponseEntity.status(status).body(response);
    }

    static HttpStatus getErrorStatus(HttpServletRequest request){
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500;
        if(status != null) {
            statusCode = (Integer) status;
        }
        return HttpStatus.resolve(statusCode);
    }

    static ErrorResponse getErrorResponse(HttpStatus httpStatus){
        String error = switch (httpStatus) {
            case NOT_FOUND -> "Bad URL. Wrong path or missing url arguments";
            case METHOD_NOT_ALLOWED -> "Wrong request method";
            case UNSUPPORTED_MEDIA_TYPE -> "Unsupported media type";
            default -> "Unknown error";
        };
        return new ErrorResponse(error);
    }
}
