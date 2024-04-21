package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.InvalidArgumentException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocValidator {

    public void validateId(String id) {
        if (id == null) throw new InvalidArgumentException("ID cannot be null");
    }

    public void validateName(String name) {
        if (name == null) throw new InvalidArgumentException("Doc name cannot be null");
        if (name.isEmpty()) throw new InvalidArgumentException("Doc name cannot be empty");
        if (name.length() > 50) throw new InvalidArgumentException("Doc name cannot be longer than 50 characters");
    }

    public void validateDocxFile(MultipartFile docxFile) {
        if (docxFile.getContentType() == null) throw new InvalidArgumentException("File has to be a DOCX-file");
        if (!docxFile.getContentType().equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            throw new InvalidArgumentException("File has to be a DOCX-file");
    }

}
