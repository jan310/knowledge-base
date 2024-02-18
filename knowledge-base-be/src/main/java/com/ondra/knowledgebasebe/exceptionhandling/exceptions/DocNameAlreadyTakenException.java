package com.ondra.knowledgebasebe.exceptionhandling.exceptions;

public class DocNameAlreadyTakenException extends RuntimeException {
    public DocNameAlreadyTakenException(String name) {
        super("A Doc with the name " + name + " already exists");
    }
}
