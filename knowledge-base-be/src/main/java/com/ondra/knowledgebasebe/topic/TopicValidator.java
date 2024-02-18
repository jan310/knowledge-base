package com.ondra.knowledgebasebe.topic;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.InvalidArgumentException;
import org.springframework.stereotype.Component;

@Component
public class TopicValidator {

    public void validateId(String id) {
        if (id == null) throw new InvalidArgumentException("Topic id cannot be null");
    }

    public void validateName(String name) {
        if (name == null) throw new InvalidArgumentException("Topic name cannot be null");
        if (name.isEmpty()) throw new InvalidArgumentException("Topic name cannot be empty");
        if (name.length() > 50) throw new InvalidArgumentException("Topic name cannot be longer than 50 characters");
    }

    public void validateIdAndName(String id, String name) {
        validateId(id);
        validateName(name);
    }

}
