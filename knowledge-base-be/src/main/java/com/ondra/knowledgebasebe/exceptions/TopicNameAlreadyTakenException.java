package com.ondra.knowledgebasebe.exceptions;

public class TopicNameAlreadyTakenException extends RuntimeException {
    public TopicNameAlreadyTakenException(String name) {
        super("A Topic with the name " + name + " already exists");
    }
}
