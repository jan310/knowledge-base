package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.FileConversionException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.TopicNotFoundException;
import com.ondra.knowledgebasebe.topic.TopicRepository;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
public class DocService {

    private final RestClient restClient;
    private final DocRepository docRepository;
    private final TopicRepository topicRepository;

    public DocService(RestClient restClient, DocRepository docRepository, TopicRepository topicRepository) {
        this.restClient = restClient;
        this.docRepository = docRepository;
        this.topicRepository = topicRepository;
    }

    public DocDto addDoc(String topicId, String name, MultipartFile docxFile) {
        if (!topicRepository.existsById(topicId)) throw new TopicNotFoundException(topicId);
        if (docRepository.existsByName(name)) throw new DocNameAlreadyTakenException(name);
        Binary docxBinary = convertMultipartFileToBinary(docxFile);
        Binary pdfBinary = convertDocxMultipartFileToPdfBinary(docxFile);
        Doc doc = new Doc(null, topicId, name, docxBinary, pdfBinary);
        return docRepository.save(doc).toDto();
    }

    public List<DocDto> getAllDocs() {
        return docRepository.findAllAndExcludeBinaryData().stream().map(Doc::toDto).collect(Collectors.toList());
    }

    public byte[] getPdf(String id) {
        Doc doc = docRepository.findPdfFileByDocId(id).orElseThrow(() -> new DocNotFoundException(id));
        return doc.getPdfFile().getData();
    }

    public byte[] getDocx(String id) {
        Doc doc = docRepository.findDocxFileByDocId(id).orElseThrow(() -> new DocNotFoundException(id));
        return doc.getDocxFile().getData();
    }

    public DocDto renameDoc(String id, String name) {
        Doc oldDoc = docRepository.findById(id).orElseThrow(() -> new DocNotFoundException(id));
        if (docRepository.existsByName(name)) throw new DocNameAlreadyTakenException(name);
        return docRepository.save(new Doc(id, oldDoc.getTopicId(), name, oldDoc.getDocxFile(), oldDoc.getPdfFile())).toDto();
    }

    public DocDto replaceFile(String id, MultipartFile docxFile) {
        Doc oldDoc = docRepository.findById(id).orElseThrow(() -> new DocNotFoundException(id));
        Binary docxBinary = convertMultipartFileToBinary(docxFile);
        Binary pdfBinary = convertDocxMultipartFileToPdfBinary(docxFile);
        Doc newDoc = new Doc(id, oldDoc.getTopicId(), oldDoc.getName(), docxBinary, pdfBinary);
        return docRepository.save(newDoc).toDto();
    }

    public void deleteDoc(String id) {
        if (!docRepository.existsById(id)) throw new DocNotFoundException(id);
        docRepository.deleteById(id);
    }

    public void deleteDocsByTopicId(String topicId) {
        if (!topicRepository.existsById(topicId)) throw new TopicNotFoundException(topicId);
        docRepository.deleteAllByTopicId(topicId);
    }

    private Binary convertMultipartFileToBinary(MultipartFile file) {
        try {
            return new Binary(file.getBytes());
        } catch (IOException ex) {
            throw new FileConversionException("Reading Bytes from DOCX-MultipartFile not possible");
        }
    }

    private Binary convertDocxMultipartFileToPdfBinary(MultipartFile docxFile) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", docxFile.getResource());
        try {
            byte[] pdfBytes = restClient
                .post()
                .uri("/forms/libreoffice/convert")
                .contentType(MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(byte[].class);
            return new Binary(pdfBytes);
        } catch (Exception ex) {
            throw new FileConversionException("Converting DOCX to PDF not possible");
        }
    }

}
