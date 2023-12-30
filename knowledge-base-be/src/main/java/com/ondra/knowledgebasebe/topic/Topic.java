package com.ondra.knowledgebasebe.topic;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "topics")
public class Topic {

    @Id
    private final String id;
    private final String name;

    public Topic(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TopicDto toDto() {
        return new TopicDto(id, name);
    }
}
