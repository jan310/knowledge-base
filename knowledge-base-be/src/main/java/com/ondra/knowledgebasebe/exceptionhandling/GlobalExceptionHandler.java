package com.ondra.knowledgebasebe.exceptionhandling;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.ConstraintViolationException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.FileConversionException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.IndexCardNotFoundException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.InvalidArgumentException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.TopicNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.TopicNotFoundException;
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
        logger.info("An incoming HTTP request (Method: " + request.getMethod() + " | Path: " + request.getRequestURI() +
            " ) was rejected: " + ex.getMessage());
    }

    @ExceptionHandler({DocNotFoundException.class, IndexCardNotFoundException.class, TopicNotFoundException.class})
    @ResponseStatus(NOT_FOUND)
    public void handleNotFoundExceptions(RuntimeException ex, HttpServletRequest request) {
        logger.warn("An incoming HTTP request (Method: " + request.getMethod() + " | Path: " + request.getRequestURI() +
            " ) was rejected: " + ex.getMessage());
    }

    @ExceptionHandler({
        ConstraintViolationException.class,
        DocNameAlreadyTakenException.class,
        FileConversionException.class,
        TopicNameAlreadyTakenException.class
    })
    @ResponseStatus(CONFLICT)
    public void handleConflictExceptions(RuntimeException ex, HttpServletRequest request) {
        logger.warn("An incoming HTTP request (Method: " + request.getMethod() + " | Path: " + request.getRequestURI() +
            " ) was rejected: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public void handleOtherExceptions(Exception ex, HttpServletRequest request) {
        logger.error("An incoming HTTP request (Method: " + request.getMethod() + " | Path: " + request.getRequestURI() +
            " ) was rejected: " + ex.getMessage());
    }

}
