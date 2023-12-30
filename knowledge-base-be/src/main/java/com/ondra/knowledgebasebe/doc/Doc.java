package com.ondra.knowledgebasebe.doc;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "docs")
public class Doc {

    @Id
    private final String id;
    private final String topicId;
    private final String name;
    private final Binary docxFile;
    private final Binary pdfFile;

    public Doc(String id, String topicId, String name, Binary docxFile, Binary pdfFile) {
        this.id = id;
        this.topicId = topicId;
        this.name = name;
        this.docxFile = docxFile;
        this.pdfFile = pdfFile;
    }

    public String getId() {
        return id;
    }

    public String getTopicId() {
        return topicId;
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
        return new DocDto(id, topicId, name);
    }

}
