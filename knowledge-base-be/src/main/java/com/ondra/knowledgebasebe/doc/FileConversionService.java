package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.FileConversionException;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
public class FileConversionService {

    private final RestClient restClient;

    public FileConversionService(RestClient restClient) {
        this.restClient = restClient;
    }

    public Binary convertMultipartFileToBinary(MultipartFile file) {
        try {
            return new Binary(file.getBytes());
        } catch (IOException ex) {
            throw new FileConversionException("Reading Bytes from DOCX-MultipartFile not possible");
        }
    }

    public Binary convertDocxMultipartFileToPdfBinary(MultipartFile docxFile) {
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
