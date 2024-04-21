package com.ondra.knowledgebasebe.exceptionhandling.exceptions;

public class DocNameAlreadyTakenException extends RuntimeException {
    public DocNameAlreadyTakenException(String name, String userId) {
        super("A Doc with the name '" + name + "' already exists for user '" + userId + "'");
    }
}
