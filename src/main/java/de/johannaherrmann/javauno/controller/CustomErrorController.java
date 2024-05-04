package de.johannaherrmann.javauno.controller;

import de.johannaherrmann.javauno.controller.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CustomErrorController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        HttpStatus httpStatus = ErrorResponseUtil.getErrorStatus(request);
        ErrorResponse errorResponse = ErrorResponseUtil.getErrorResponse(httpStatus);
        LOGGER.error("Request error: status:{}; message:{}", httpStatus.value(), errorResponse.getMessage());
        if("head".equalsIgnoreCase(request.getMethod())){
            return ResponseEntity.status(httpStatus).build();
        }
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
