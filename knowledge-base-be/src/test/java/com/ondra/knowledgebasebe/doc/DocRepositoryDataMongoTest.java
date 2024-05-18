package com.ondra.knowledgebasebe.doc;

import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
public class DocRepositoryDataMongoTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private DocRepository docRepository;

    private final Doc testDoc = new Doc("1", "1", "Java", new Binary(new byte[]{1}), new Binary(new byte[]{2}));

    @BeforeEach
    void setUp() {
        docRepository.deleteAll();
        docRepository.save(testDoc);
    }

    @Nested
    class ExistsByUserIdAndName {

        @Test
        public void shouldReturnTrueWhenIdAndNameIsPresent() {
            boolean exists = docRepository.existsByUserIdAndName("1", "Java");
            assertThat(exists).isTrue();
        }

        @Test
        public void shouldReturnFalseWhenIdIsNotPresent() {
            boolean exists = docRepository.existsByUserIdAndName("2", "Java");
            assertThat(exists).isFalse();
        }

        @Test
        public void shouldReturnFalseWhenNameIsNotPresent() {
            boolean exists = docRepository.existsByUserIdAndName("1", "Kotlin");
            assertThat(exists).isFalse();
        }

    }

    @Nested
    class ExistsByIdAndUserId {

        @Test
        public void shouldReturnTrueWhenIdAndUserIdIsPresent() {
            boolean exists = docRepository.existsByIdAndUserId("1", "1");
            assertThat(exists).isTrue();
        }

        @Test
        public void shouldReturnFalseWhenIdIsNotPresent() {
            boolean exists = docRepository.existsByIdAndUserId("2", "1");
            assertThat(exists).isFalse();
        }

        @Test
        public void shouldReturnFalseWhenUserIdIsNotPresent() {
            boolean exists = docRepository.existsByIdAndUserId("1", "2");
            assertThat(exists).isFalse();
        }

    }

    @Nested
    class FindByIdAndUserId {

        @Test
        public void shouldReturnDocOptionalWhenIdAndUserIdIsPresent() {
            Optional<Doc> docOptional = docRepository.findByIdAndUserId("1", "1");
            assertThat(docOptional.isPresent()).isTrue();
            assertThat(docOptional.get()).usingRecursiveComparison().isEqualTo(testDoc);
        }

        @Test
        public void shouldReturnEmptyOptionalWhenIdIsNotPresent() {
            Optional<Doc> docOptional = docRepository.findByIdAndUserId("2", "1");
            assertThat(docOptional.isPresent()).isFalse();
        }

        @Test
        public void shouldReturnEmptyOptionalWhenUserIdIsNotPresent() {
            Optional<Doc> docOptional = docRepository.findByIdAndUserId("1", "2");
            assertThat(docOptional.isPresent()).isFalse();
        }

    }

    @Nested
    class FindAllByUserIdAndExcludeBinaryData {

        @Test
        public void shouldReturnDocListWhenUserIdIsPresent() {
            List<Doc> docList = docRepository.findAllByUserIdAndExcludeBinaryData("1");
            assertThat(docList.size()).isEqualTo(1);
            assertThat(docList.getFirst()).usingRecursiveComparison().isEqualTo(
                new Doc("1", "1", "Java", null, null)
            );
        }

        @Test
        public void shouldReturnEmptyListWhenUserIdIsNotPresent() {
            List<Doc> docList = docRepository.findAllByUserIdAndExcludeBinaryData("2");
            assertThat(docList.isEmpty()).isTrue();
        }

    }

    @Nested
    class FindPdfFileByDocIdAndUserId {

        @Test
        public void shouldReturnDocOptionalWhenIdAndUserIdIsPresent() {
            Optional<Doc> docOptional = docRepository.findPdfFileByDocIdAndUserId("1", "1");
            assertThat(docOptional.isPresent()).isTrue();
            assertThat(docOptional.get()).usingRecursiveComparison().isEqualTo(
                new Doc("1", null, null, null, new Binary(new byte[]{2}))
            );
        }

        @Test
        public void shouldReturnEmptyOptionalWhenIdIsNotPresent() {
            Optional<Doc> docOptional = docRepository.findPdfFileByDocIdAndUserId("2", "1");
            assertThat(docOptional.isPresent()).isFalse();
        }

        @Test
        public void shouldReturnEmptyOptionalWhenUserIdIsNotPresent() {
            Optional<Doc> docOptional = docRepository.findPdfFileByDocIdAndUserId("1", "2");
            assertThat(docOptional.isPresent()).isFalse();
        }

    }

    @Nested
    class FindDocxFileByDocIdAndUserId {

        @Test
        public void shouldReturnDocOptionalWhenIdAndUserIdIsPresent() {
            Optional<Doc> docOptional = docRepository.findDocxFileByDocIdAndUserId("1", "1");
            assertThat(docOptional.isPresent()).isTrue();
            assertThat(docOptional.get()).usingRecursiveComparison().isEqualTo(
                new Doc("1", null, null, new Binary(new byte[]{1}), null)
            );
        }

        @Test
        public void shouldReturnEmptyOptionalWhenIdIsNotPresent() {
            Optional<Doc> docOptional = docRepository.findDocxFileByDocIdAndUserId("2", "1");
            assertThat(docOptional.isPresent()).isFalse();
        }

        @Test
        public void shouldReturnEmptyOptionalWhenUserIdIsNotPresent() {
            Optional<Doc> docOptional = docRepository.findDocxFileByDocIdAndUserId("1", "2");
            assertThat(docOptional.isPresent()).isFalse();
        }

    }

}
