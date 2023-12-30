package com.ondra.knowledgebasebe.exceptions;

public class ConstraintViolationException extends RuntimeException{
    public ConstraintViolationException(String message) {
        super(message);
    }
}
