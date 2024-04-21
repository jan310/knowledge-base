package com.ondra.knowledgebasebe.doc;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "docs")
public class Doc {

    @Id
    private final String id;
    private final String userId;
    private final String name;
    private final Binary docxFile;
    private final Binary pdfFile;

    public Doc(String id, String userId, String name, Binary docxFile, Binary pdfFile) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.docxFile = docxFile;
        this.pdfFile = pdfFile;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Binary getDocxFile() {
        return docxFile;
    }

    public Binary getPdfFile() {
        return pdfFile;
    }

    public DocDto toDto() {
        return new DocDto(id, userId, name);
    }

}
