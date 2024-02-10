package com.ondra.knowledgebasebe.exceptions;

public class IndexCardNotFoundException extends RuntimeException {
    public IndexCardNotFoundException(String id) {
        super("An index card with the id " + id + " does not exist");
    }
}
