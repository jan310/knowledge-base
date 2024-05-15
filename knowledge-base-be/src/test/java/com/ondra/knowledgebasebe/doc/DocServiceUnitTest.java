package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import org.bson.types.Binary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocServiceUnitTest {

    @Mock
    private DocRepository docRepository;

    @Mock
    private FileConversionService fileConversionService;

    @InjectMocks
    private DocService docService;

    @Captor
    private ArgumentCaptor<Doc> docCaptor;

    private static final String ID = "1";
    private static final String USER_ID = "1";
    private static final String NAME = "Java";
    private static final Binary BINARY_DOCX = new Binary(new byte[]{1});
    private static final Binary BINARY_PDF = new Binary(new byte[]{2});
    private static final MultipartFile MULTIPART_FILE = new MockMultipartFile("docxFile", new byte[]{});

    @Nested
    class AddDoc {

        @Test
        void shouldReturnAddedDoc() {
            when(docRepository.existsByUserIdAndName(any(), any())).thenReturn(false);
            when(fileConversionService.convertDocxMultipartFileToDocxBinary(any())).thenReturn(BINARY_DOCX);
            when(fileConversionService.convertDocxMultipartFileToPdfBinary(any())).thenReturn(BINARY_PDF);
            when(docRepository.save(any())).thenReturn(new Doc(ID, USER_ID, NAME, BINARY_DOCX, BINARY_PDF));

            DocDto result = docService.addDoc(USER_ID, NAME, MULTIPART_FILE);

            assertThat(result).isEqualTo(new DocDto(ID, USER_ID, NAME));
            verify(docRepository, times(1)).existsByUserIdAndName(USER_ID, NAME);
            verify(fileConversionService, times(1)).convertDocxMultipartFileToDocxBinary(MULTIPART_FILE);
            verify(fileConversionService, times(1)).convertDocxMultipartFileToPdfBinary(MULTIPART_FILE);
            verify(docRepository, times(1)).save(docCaptor.capture());
            assertThat(docCaptor.getValue()).usingRecursiveComparison().isEqualTo(new Doc(null, USER_ID, NAME, BINARY_DOCX, BINARY_PDF));
        }

        @Test
        void shouldFailIfDocNameIsAlreadyTaken() {
            when(docRepository.existsByUserIdAndName(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> docService.addDoc(USER_ID, NAME, MULTIPART_FILE)).isInstanceOf(DocNameAlreadyTakenException.class);

            verify(docRepository, times(1)).existsByUserIdAndName(USER_ID, NAME);
            verify(fileConversionService, times(0)).convertDocxMultipartFileToDocxBinary(any());
            verify(fileConversionService, times(0)).convertDocxMultipartFileToPdfBinary(any());
            verify(docRepository, times(0)).save(any());
        }

    }

    @Nested
    class GetAllDocs {

        @Test
        void shouldReturnAllDocs() {
            ArrayList<Doc> docs = new ArrayList<>() {{ add(new Doc(ID, USER_ID, NAME, BINARY_DOCX, BINARY_PDF)); }};
            when(docRepository.findAllByUserIdAndExcludeBinaryData(any())).thenReturn(docs);

            List<DocDto> result = docService.getAllDocs(USER_ID);

            assertThat(result).isEqualTo(docs.stream().map(Doc::toDto).toList());
            verify(docRepository, times(1)).findAllByUserIdAndExcludeBinaryData(USER_ID);
        }

    }

    @Nested
    class GetPdf {

        @Test
        void shouldReturnPdf() {
            Doc doc = new Doc(ID, null, null, null, BINARY_PDF);
            when(docRepository.findPdfFileByDocIdAndUserId(any(), any())).thenReturn(Optional.of(doc));

            byte[] result = docService.getPdf(ID, USER_ID);

            assertThat(result).isEqualTo(doc.getPdfFile().getData());
            verify(docRepository, times(1)).findPdfFileByDocIdAndUserId(ID, USER_ID);
        }

        @Test
        void shouldFailIfDocDoesNotExist() {
            when(docRepository.findPdfFileByDocIdAndUserId(any(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> docService.getPdf(ID, USER_ID)).isInstanceOf(DocNotFoundException.class);

            verify(docRepository, times(1)).findPdfFileByDocIdAndUserId(ID, USER_ID);
        }

    }

    @Nested
    class GetDocx {

        @Test
        void shouldReturnDocx() {
            Doc doc = new Doc(ID, null, null, BINARY_DOCX, null);
            when(docRepository.findDocxFileByDocIdAndUserId(any(), any())).thenReturn(Optional.of(doc));

            byte[] result = docService.getDocx(ID, USER_ID);

            assertThat(result).isEqualTo(doc.getDocxFile().getData());
            verify(docRepository, times(1)).findDocxFileByDocIdAndUserId(ID, USER_ID);
        }

        @Test
        void shouldFailIfDocDoesNotExist() {
            when(docRepository.findDocxFileByDocIdAndUserId(any(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> docService.getDocx(ID, USER_ID)).isInstanceOf(DocNotFoundException.class);

            verify(docRepository, times(1)).findDocxFileByDocIdAndUserId(ID, USER_ID);
        }

    }

    @Nested
    class RenameDoc {

        @Test
        void shouldReturnRenamedDoc() {
            Doc oldDoc = new Doc(ID, USER_ID, "Kotlin", BINARY_DOCX, BINARY_PDF);
            Doc newDoc = new Doc(ID, USER_ID, NAME, BINARY_DOCX, BINARY_PDF);
            when(docRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(oldDoc));
            when(docRepository.existsByUserIdAndName(any(), any())).thenReturn(false);
            when(docRepository.save(any())).thenReturn(newDoc);

            DocDto result = docService.renameDoc(ID, USER_ID, NAME);

            assertThat(result).isEqualTo(new DocDto(ID, USER_ID, NAME));
            verify(docRepository, times(1)).findByIdAndUserId(ID, USER_ID);
            verify(docRepository, times(1)).existsByUserIdAndName(USER_ID, NAME);
            verify(docRepository, times(1)).save(docCaptor.capture());
            assertThat(docCaptor.getValue()).usingRecursiveComparison().isEqualTo(newDoc);
        }

        @Test
        void shouldFailIfDocDoesNotExist() {
            when(docRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> docService.renameDoc(ID, USER_ID, NAME)).isInstanceOf(DocNotFoundException.class);

            verify(docRepository, times(1)).findByIdAndUserId(ID, USER_ID);
            verify(docRepository, times(0)).existsByUserIdAndName(any(), any());
            verify(docRepository, times(0)).save(any());
        }

        @Test
        void shouldFailIfDocNameIsAlreadyTaken() {
            Doc oldDoc = new Doc(ID, USER_ID, "Kotlin", BINARY_DOCX, BINARY_PDF);
            when(docRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(oldDoc));
            when(docRepository.existsByUserIdAndName(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> docService.renameDoc(ID, USER_ID, NAME)).isInstanceOf(DocNameAlreadyTakenException.class);

            verify(docRepository, times(1)).findByIdAndUserId(ID, USER_ID);
            verify(docRepository, times(1)).existsByUserIdAndName(USER_ID, NAME);
            verify(docRepository, times(0)).save(any());
        }

    }

    @Nested
    class ReplaceFile {

        @Test
        void shouldReturnDoc() {
            Doc oldDoc = new Doc(ID, USER_ID, NAME, new Binary(new byte[]{3}), new Binary(new byte[]{4}));
            Doc newDoc = new Doc(ID, USER_ID, NAME, BINARY_DOCX, BINARY_PDF);
            when(docRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(oldDoc));
            when(fileConversionService.convertDocxMultipartFileToDocxBinary(any())).thenReturn(BINARY_DOCX);
            when(fileConversionService.convertDocxMultipartFileToPdfBinary(any())).thenReturn(BINARY_PDF);
            when(docRepository.save(any())).thenReturn(newDoc);

            DocDto result = docService.replaceFile(ID, USER_ID, MULTIPART_FILE);

            assertThat(result).isEqualTo(new DocDto(ID, USER_ID, NAME));
            verify(docRepository, times(1)).findByIdAndUserId(ID, USER_ID);
            verify(fileConversionService, times(1)).convertDocxMultipartFileToDocxBinary(MULTIPART_FILE);
            verify(fileConversionService, times(1)).convertDocxMultipartFileToPdfBinary(MULTIPART_FILE);
            verify(docRepository, times(1)).save(docCaptor.capture());
            assertThat(docCaptor.getValue()).usingRecursiveComparison().isEqualTo(newDoc);
        }

        @Test
        void shouldFailIfDocDoesNotExist() {
            when(docRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> docService.replaceFile(ID, USER_ID, MULTIPART_FILE)).isInstanceOf(DocNotFoundException.class);

            verify(docRepository, times(1)).findByIdAndUserId(ID, USER_ID);
            verify(fileConversionService, times(0)).convertDocxMultipartFileToDocxBinary(any());
            verify(fileConversionService, times(0)).convertDocxMultipartFileToPdfBinary(any());
            verify(docRepository, times(0)).save(any());
        }

    }

    @Nested
    class DeleteDoc {

        @Test
        void shouldReturnVoid() {
            when(docRepository.existsByIdAndUserId(any(), any())).thenReturn(true);
            doNothing().when(docRepository).deleteByIdAndUserId(any(), any());

            docService.deleteDoc(ID, USER_ID);

            verify(docRepository, times(1)).existsByIdAndUserId(ID, USER_ID);
            verify(docRepository, times(1)).deleteByIdAndUserId(ID, USER_ID);
        }

        @Test
        void shouldFailIfDocDoesNotExist() {
            when(docRepository.existsByIdAndUserId(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> docService.deleteDoc(ID, USER_ID)).isInstanceOf(DocNotFoundException.class);

            verify(docRepository, times(1)).existsByIdAndUserId(ID, USER_ID);
            verify(docRepository, times(0)).deleteByIdAndUserId(any(), any());
        }

    }

}
