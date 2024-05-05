package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.ondra.knowledgebasebe.SecurityTestData.BEARER_TOKEN_USER_1;
import static com.ondra.knowledgebasebe.SecurityTestData.USER_ID_1;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocController.class)
class DocControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocValidator docValidator;

    @MockBean
    private DocService docService;

    private static MockMultipartFile mockMultipartFile;

    @BeforeAll
    static void setup() throws IOException {
        mockMultipartFile = new MockMultipartFile(
            "docxFile",
            "file.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            Files.readAllBytes(Paths.get("src/test/resources/test1.docx"))
        );
    }

    @Nested
    class AddDoc {

        @Test
        void shouldReturnAddedDoc() throws Exception {
            when(docService.addDoc(USER_ID_1, "Java", mockMultipartFile)).thenReturn(new DocDto("1", USER_ID_1, "Java"));

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/v1/docs")
                .file(mockMultipartFile)
                .param("name", "Java")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(USER_ID_1))
                .andExpect(jsonPath("$.name").value("Java"));
        }

        @Test
        void shouldReturnBadRequestIfNameIsEmpty() throws Exception {
            doThrow(new InvalidArgumentException("Doc name cannot be empty")).when(docValidator).validateName("");

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/v1/docs")
                .file(mockMultipartFile)
                .param("name", "")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
        }

        @Test
        void shouldReturnConflictIfNameIsAlreadyTaken() throws Exception {
            doThrow(new DocNameAlreadyTakenException("Java", USER_ID_1)).when(docService).addDoc(USER_ID_1, "Java", mockMultipartFile);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/v1/docs")
                .file(mockMultipartFile)
                .param("name", "Java")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().string(""));
        }

    }

    @Nested
    class GetAllDocs {

        @Test
        void shouldReturnAllDocs() throws Exception {
            List<DocDto> docs = new ArrayList<>();
            docs.add(new DocDto("1", USER_ID_1, "Java"));
            docs.add(new DocDto("2", USER_ID_1, "Kotlin"));

            when(docService.getAllDocs(USER_ID_1)).thenReturn(docs);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].userId").value(USER_ID_1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].userId").value(USER_ID_1))
                .andExpect(jsonPath("$[1].name").value("Kotlin"));
        }

    }

    @Nested
    class GetPdf {

        @Test
        void shouldReturnPdf() throws Exception {
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));

            when(docService.getPdf("1", USER_ID_1)).thenReturn(pdfFileBytes);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/1/pdf")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(pdfFileBytes));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException("1", USER_ID_1)).when(docService).getPdf("1", USER_ID_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/1/pdf")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        }

    }

    @Nested
    class GetDocx {

        @Test
        void shouldReturnDocx() throws Exception {
            byte[] docxFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.docx"));

            when(docService.getDocx("1", USER_ID_1)).thenReturn(docxFileBytes);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/1/docx")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(content().bytes(docxFileBytes));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException("1", USER_ID_1)).when(docService).getDocx("1", USER_ID_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/1/docx")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        }

    }

    @Nested
    class RenameDoc {

        @Test
        void shouldReturnRenamedDoc() throws Exception {
            when(docService.renameDoc("1", USER_ID_1, "Java")).thenReturn(new DocDto("1", USER_ID_1, "Java"));

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch("/api/v1/docs/1/rename")
                .param("name", "Java")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.userId").value(USER_ID_1))
                .andExpect(jsonPath("$.name").value("Java"));
        }

        @Test
        void shouldReturnConflictIfNameIsAlreadyTaken() throws Exception {
            doThrow(new DocNameAlreadyTakenException("Java", USER_ID_1)).when(docService).renameDoc("1", USER_ID_1, "Java");

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch("/api/v1/docs/1/rename")
                .param("name", "Java")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isConflict())
                .andExpect(content().string(""));
        }

    }

    @Nested
    class ReplaceFile {

        @Test
        void shouldReturnDoc() throws Exception {
            when(docService.replaceFile("1", USER_ID_1, mockMultipartFile)).thenReturn(new DocDto("1", USER_ID_1, "Java"));

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart(PATCH ,"/api/v1/docs/1/replace-file")
                .file(mockMultipartFile)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.userId").value(USER_ID_1))
                .andExpect(jsonPath("$.name").value("Java"));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException("1", USER_ID_1)).when(docService).replaceFile("1", USER_ID_1, mockMultipartFile);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart(PATCH ,"/api/v1/docs/1/replace-file")
                .file(mockMultipartFile)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        }

    }

    @Nested
    class DeleteDoc {

        @Test
        void shouldReturnEmptyResponseBody() throws Exception {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/v1/docs/1")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException("1", USER_ID_1)).when(docService).deleteDoc("1", USER_ID_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/v1/docs/1")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        }

    }

}