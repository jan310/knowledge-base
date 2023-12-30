package com.ondra.knowledgebasebe.exceptions;

public class DocNotFoundException extends RuntimeException {
    public DocNotFoundException(String id) {
        super("A doc with the id " + id + " does not exist");
    }
}
