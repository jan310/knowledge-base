package com.ondra.knowledgebasebe.doc;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DocRepository extends MongoRepository<Doc,String> {

    boolean existsByUserIdAndName(String userId, String name);

    boolean existsByIdAndUserId(String id, String userId);

    Optional<Doc> findByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);

    @Query(value = "{ 'userId': ?0 }", fields = "{ '_id': 1, 'userId': 1, 'name': 1 }")
    List<Doc> findAllByUserIdAndExcludeBinaryData(String userId);

    @Query(value = "{ '_id': ?0, 'userId': ?1 }", fields = "{ 'pdfFile': 1 }")
    Optional<Doc> findPdfFileByDocIdAndUserId(String id, String userId);

    @Query(value = "{ '_id': ?0, 'userId': ?1 }", fields = "{ 'docxFile': 1 }")
    Optional<Doc> findDocxFileByDocIdAndUserId(String id, String userId);

}
