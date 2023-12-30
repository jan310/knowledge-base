package com.ondra.knowledgebasebe.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public String handleValidationExceptions(InvalidArgumentException ex, HttpServletRequest request) {
        String errorMessage = "Invalid request content: " + ex.getMessage();
        logger.info(errorMessage + " (Endpoint: " + request.getMethod() + " " + request.getRequestURI() + ")");
        return errorMessage;
    }

}
