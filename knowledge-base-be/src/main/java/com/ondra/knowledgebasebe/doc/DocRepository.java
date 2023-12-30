package com.ondra.knowledgebasebe.doc;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocRepository extends MongoRepository<Doc,String> {

    boolean existsByName(String name);

    boolean existsByTopicId(String topicId);

    void deleteAllByTopicId(String topicId);

    @Query(value = "{}", fields = "{ '_id': 1, 'topicId': 1, 'name': 1 }")
    List<Doc> findAllAndExcludeBinaryData();

    @Query(value = "{ '_id': ?0 }", fields = "{ 'pdfFile': 1 }")
    Optional<Doc> findPdfFileByDocId(String id);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'docxFile': 1 }")
    Optional<Doc> findDocxFileByDocId(String id);

}
