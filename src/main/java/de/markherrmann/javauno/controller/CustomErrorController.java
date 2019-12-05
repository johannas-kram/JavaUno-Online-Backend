package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public @ResponseBody
    ErrorResponse handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status == null) {
            return new ErrorResponse("unknown error");
        }

        Integer statusCode = Integer.valueOf(status.toString());
        if(statusCode == HttpStatus.NOT_FOUND.value()) {
            return new ErrorResponse("error: wrong path or missing url parts");
        } else if(statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
            return new ErrorResponse("error: wrong request method");
        } else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return new ErrorResponse("error: internal server error");
        } else if(statusCode == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()) {
            return new ErrorResponse("error: unsupported media type");
        }
        
        return new ErrorResponse("unknown error");
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
