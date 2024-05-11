package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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

    private static final String ID_1 = "1";
    private static final String ID_2 = "2";
    private static final String NAME_1 = "Java";
    private static final String NAME_2 = "Kotlin";
    private static final MockMultipartFile MULTIPART_FILE = new MockMultipartFile("docxFile", new byte[]{});

    @Nested
    class AddDoc {

        @Test
        void shouldReturnAddedDoc() throws Exception {
            when(docService.addDoc(USER_ID_1, NAME_1, MULTIPART_FILE)).thenReturn(new DocDto(ID_1, USER_ID_1, NAME_1));

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/v1/docs")
                .file(MULTIPART_FILE)
                .param("name", NAME_1)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_1))
                .andExpect(jsonPath("$.userId").value(USER_ID_1))
                .andExpect(jsonPath("$.name").value(NAME_1));
        }

        @Test
        void shouldReturnBadRequestIfNameIsEmpty() throws Exception {
            doThrow(new InvalidArgumentException("Doc name cannot be empty")).when(docValidator).validateName("");

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/v1/docs")
                .file(MULTIPART_FILE)
                .param("name", "")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
        }

        @Test
        void shouldReturnConflictIfNameIsAlreadyTaken() throws Exception {
            doThrow(new DocNameAlreadyTakenException(NAME_1, USER_ID_1)).when(docService).addDoc(USER_ID_1, NAME_1, MULTIPART_FILE);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart("/api/v1/docs")
                .file(MULTIPART_FILE)
                .param("name", NAME_1)
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
            docs.add(new DocDto(ID_1, USER_ID_1, NAME_1));
            docs.add(new DocDto(ID_2, USER_ID_1, NAME_2));

            when(docService.getAllDocs(USER_ID_1)).thenReturn(docs);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID_1))
                .andExpect(jsonPath("$[0].userId").value(USER_ID_1))
                .andExpect(jsonPath("$[0].name").value(NAME_1))
                .andExpect(jsonPath("$[1].id").value(ID_2))
                .andExpect(jsonPath("$[1].userId").value(USER_ID_1))
                .andExpect(jsonPath("$[1].name").value(NAME_2));
        }

    }

    @Nested
    class GetPdf {

        @Test
        void shouldReturnPdf() throws Exception {
            byte[] pdfFileBytes = Files.readAllBytes(Paths.get("src/test/resources/test1.pdf"));

            when(docService.getPdf(ID_1, USER_ID_1)).thenReturn(pdfFileBytes);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/" + ID_1 + "/pdf")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(pdfFileBytes));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException(ID_1, USER_ID_1)).when(docService).getPdf(ID_1, USER_ID_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/" + ID_1 + "/pdf")
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

            when(docService.getDocx(ID_1, USER_ID_1)).thenReturn(docxFileBytes);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/" + ID_1 + "/docx")
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(content().bytes(docxFileBytes));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException(ID_1, USER_ID_1)).when(docService).getDocx(ID_1, USER_ID_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/api/v1/docs/" + ID_1 + "/docx")
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
            when(docService.renameDoc(ID_1, USER_ID_1, NAME_1)).thenReturn(new DocDto(ID_1, USER_ID_1, NAME_1));

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch("/api/v1/docs/" + ID_1 + "/rename")
                .param("name", NAME_1)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(ID_1))
                .andExpect(jsonPath("$.userId").value(USER_ID_1))
                .andExpect(jsonPath("$.name").value(NAME_1));
        }

        @Test
        void shouldReturnConflictIfNameIsAlreadyTaken() throws Exception {
            doThrow(new DocNameAlreadyTakenException(NAME_1, USER_ID_1)).when(docService).renameDoc(ID_1, USER_ID_1, NAME_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .patch("/api/v1/docs/" + ID_1 + "/rename")
                .param("name", NAME_1)
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
            when(docService.replaceFile(ID_1, USER_ID_1, MULTIPART_FILE)).thenReturn(new DocDto(ID_1, USER_ID_1, NAME_1));

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart(PATCH ,"/api/v1/docs/" + ID_1 + "/replace-file")
                .file(MULTIPART_FILE)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(ID_1))
                .andExpect(jsonPath("$.userId").value(USER_ID_1))
                .andExpect(jsonPath("$.name").value(NAME_1));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException(ID_1, USER_ID_1)).when(docService).replaceFile(ID_1, USER_ID_1, MULTIPART_FILE);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .multipart(PATCH ,"/api/v1/docs/" + ID_1 + "/replace-file")
                .file(MULTIPART_FILE)
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
                .delete("/api/v1/docs/" + ID_1)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        }

        @Test
        void shouldReturnNotFoundIfIdDoesNotExistForUser() throws Exception {
            doThrow(new DocNotFoundException(ID_1, USER_ID_1)).when(docService).deleteDoc(ID_1, USER_ID_1);

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete("/api/v1/docs/" + ID_1)
                .header("Authorization", BEARER_TOKEN_USER_1);

            mockMvc
                .perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        }

    }

}