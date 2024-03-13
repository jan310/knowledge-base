package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.BaseIntTest;
import com.ondra.knowledgebasebe.topic.Topic;
import com.ondra.knowledgebasebe.topic.TopicRepository;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocControllerIntTest extends BaseIntTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DocRepository docRepository;
    @Autowired
    private TopicRepository topicRepository;

    static class GotenbergContainer extends GenericContainer<GotenbergContainer> {
        public GotenbergContainer(String image) {
            super(image);
        }
    }
    @Container
    static GotenbergContainer gotenbergContainer = new GotenbergContainer("gotenberg/gotenberg:7.10")
        .withExposedPorts(3000);

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("gotenberg.port", gotenbergContainer::getFirstMappedPort);
    }

    @BeforeEach
    void clearDatabase() {
        docRepository.deleteAll();
        topicRepository.deleteAll();
    }

    @Test
    void addDoc() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();

        byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test.docx"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "docxFile",
            "file.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            docxFileBytes
        );
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .multipart("/doc-api/v1/doc")
            .file(mockMultipartFile)
            .param("topicId", topicId)
            .param("name", "Garbage Collector");

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.topicId").value(topicId))
            .andExpect(jsonPath("$.name").value("Garbage Collector"));

        List<Doc> docs = docRepository.findAll();
        assertEquals(1, docs.size());
        assertEquals(topicId, docs.get(0).getTopicId());
        assertEquals("Garbage Collector", docs.get(0).getName());
        assertEquals(new Binary(docxFileBytes), docs.get(0).getDocxFile());
        assertNotEquals(null, docs.get(0).getPdfFile());
    }

    @Test
    void getAllDocs() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();
        Doc doc1 = new Doc(null, topicId, "Garbage Collector", null, null);
        Doc doc2 = new Doc(null, topicId, "Virtual Threads", null, null);
        String doc1Id = docRepository.save(doc1).getId();
        String doc2Id = docRepository.save(doc2).getId();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/doc-api/v1/docs");

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(doc1Id))
            .andExpect(jsonPath("$[0].topicId").value(topicId))
            .andExpect(jsonPath("$[0].name").value("Garbage Collector"))
            .andExpect(jsonPath("$[1].id").value(doc2Id))
            .andExpect(jsonPath("$[1].topicId").value(topicId))
            .andExpect(jsonPath("$[1].name").value("Virtual Threads"));
    }

    @Test
    void getPdf() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();
        byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test.pdf"));
        Doc doc = new Doc(null, topicId, "Garbage Collector", null, new Binary(pdfFileBytes));
        String docId = docRepository.save(doc).getId();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/doc-api/v1/doc/pdf")
            .param("id", docId);

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_PDF_VALUE))
            .andExpect(content().bytes(pdfFileBytes));
    }

    @Test
    void getDocx() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();
        byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test.docx"));
        Doc doc = new Doc(null, topicId, "Garbage Collector", new Binary(docxFileBytes), null);
        String docId = docRepository.save(doc).getId();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/doc-api/v1/doc/docx")
            .param("id", docId);

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            .andExpect(content().bytes(docxFileBytes));
    }

    @Test
    void renameDoc() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();
        Doc doc = new Doc(null, topicId, "Garbage Collector", null, null);
        String docId = docRepository.save(doc).getId();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .put("/doc-api/v1/doc/name")
            .param("id", docId)
            .param("name", "Virtual Threads");

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id").value(docId))
            .andExpect(jsonPath("$.topicId").value(topicId))
            .andExpect(jsonPath("$.name").value("Virtual Threads"));

        List<Doc> docs = docRepository.findAll();
        assertEquals(1, docs.size());
        assertEquals(docId, docs.get(0).getId());
        assertEquals("Virtual Threads", docs.get(0).getName());
    }

    @Test
    void replaceFile() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();
        Doc doc = new Doc(null, topicId, "Garbage Collector", null, null);
        String docId = docRepository.save(doc).getId();

        byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test.docx"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "docxFile",
            "file.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            docxFileBytes
        );
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .multipart(PUT ,"/doc-api/v1/doc/docx")
            .file(mockMultipartFile)
            .param("id", docId);

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id").value(docId))
            .andExpect(jsonPath("$.topicId").value(topicId))
            .andExpect(jsonPath("$.name").value("Garbage Collector"));

        List<Doc> docs = docRepository.findAll();
        assertEquals(1, docs.size());
        assertEquals(docId, docs.get(0).getId());
        assertEquals(topicId, docs.get(0).getTopicId());
        assertEquals("Garbage Collector", docs.get(0).getName());
        assertEquals(new Binary(docxFileBytes), docs.get(0).getDocxFile());
        assertNotEquals(null, docs.get(0).getPdfFile());
    }

    @Test
    void deleteDoc() throws Exception {
        String topicId = topicRepository.save(new Topic(null, "Java")).getId();
        Doc doc1 = new Doc(null, topicId, "Garbage Collector", null, null);
        Doc doc2 = new Doc(null, topicId, "Virtual Threads", null, null);
        String doc1Id = docRepository.save(doc1).getId();
        String doc2Id = docRepository.save(doc2).getId();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete("/doc-api/v1/doc")
            .param("id", doc1Id);

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isNoContent());

        List<Doc> docs = docRepository.findAll();
        assertEquals(1, docs.size());
        assertEquals(doc2Id, docs.get(0).getId());
    }

    @Test
    void deleteDocsByTopicId() throws Exception {
        String topic1Id = topicRepository.save(new Topic(null, "Java")).getId();
        String topic2Id = topicRepository.save(new Topic(null, "Kotlin")).getId();
        Doc doc1 = new Doc(null, topic1Id, "Garbage Collector", null, null);
        Doc doc2 = new Doc(null, topic1Id, "Virtual Threads", null, null);
        Doc doc3 = new Doc(null, topic2Id, "Data Classes", null, null);
        docRepository.save(doc1);
        docRepository.save(doc2);
        String doc3Id = docRepository.save(doc3).getId();

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete("/doc-api/v1/docs/by-topicId")
            .param("topicId", topic1Id);

        mockMvc
            .perform(requestBuilder)
            .andExpect(status().isNoContent());

        List<Doc> docs = docRepository.findAll();
        assertEquals(1, docs.size());
        assertEquals(doc3Id, docs.get(0).getId());
    }
}