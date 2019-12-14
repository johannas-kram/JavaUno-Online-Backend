package de.markherrmann.javauno.controller;

import de.markherrmann.javauno.controller.response.ErrorResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        HttpStatus httpStatus = ErrorResponseUtil.getCustomErrorStatus(request);
        return ErrorResponseUtil.getCustomErrorResponse(httpStatus);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
