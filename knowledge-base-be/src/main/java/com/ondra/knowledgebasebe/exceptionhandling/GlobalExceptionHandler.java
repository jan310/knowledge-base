package com.ondra.knowledgebasebe.exceptionhandling;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.FileConversionException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.InvalidArgumentException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public void handleValidationExceptions(InvalidArgumentException ex, HttpServletRequest request) {
        logger.info(getLogMessage(request.getMethod(), request.getRequestURI(), ex.getMessage()));
    }

    @ExceptionHandler(DocNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public void handleNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {
        logger.warn(getLogMessage(request.getMethod(), request.getRequestURI(), ex.getMessage()));
    }

    @ExceptionHandler({DocNameAlreadyTakenException.class, FileConversionException.class})
    @ResponseStatus(CONFLICT)
    public void handleConflictExceptions(RuntimeException ex, HttpServletRequest request) {
        logger.warn(getLogMessage(request.getMethod(), request.getRequestURI(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public void handleOtherExceptions(Exception ex, HttpServletRequest request) {
        logger.error(getLogMessage(request.getMethod(), request.getRequestURI(), ex.getMessage()));
    }

    private String getLogMessage(String requestMethod, String requestPath, String message) {
        return "An incoming HTTP request [" + requestMethod + " " + requestPath + "] was rejected: " + message;
    }

}
