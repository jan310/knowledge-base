package com.ondra.knowledgebasebe.indexcard;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IndexCardRepository extends MongoRepository<IndexCard, String> {

    boolean existsByTopicId(String topicId);

    void deleteAllByTopicId(String topicId);

    @Query(value = "{ 'topicId': ?0 }", fields = "{'_id': 1, 'topicId': 1, 'question': 1, 'answer': 1, 'hasAnswerImage': 1, 'isMarked': 1}")
    List<IndexCard> findAllByTopicIdAndExcludeAnswerImages(String topicId);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'answerImage': 1 }")
    Optional<IndexCard> findAnswerImageByIndexCardId(String id);

}
