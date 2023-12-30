package com.ondra.knowledgebasebe.topic;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TopicRepository extends MongoRepository<Topic,String> {

    boolean existsByName(String name);

}
