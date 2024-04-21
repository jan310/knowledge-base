package com.ondra.knowledgebasebe.exceptionhandling.exceptions;

public class DocNotFoundException extends RuntimeException {
    public DocNotFoundException(String id, String userId) {
        super("A doc with the id '" + id + "' does not exist for user '" + userId + "'");
    }
}
