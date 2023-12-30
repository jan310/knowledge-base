package com.ondra.knowledgebasebe.exceptions;

public class TopicNotFoundException extends RuntimeException {
    public TopicNotFoundException(String id) {
        super("A topic with the id " + id + " does not exist");
    }
}
