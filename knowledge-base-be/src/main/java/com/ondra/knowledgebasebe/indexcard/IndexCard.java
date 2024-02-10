package com.ondra.knowledgebasebe.indexcard;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class IndexCard {

    @Id
    private String id;
    private final String topicId;
    private final String question;
    private final String answer;
    private final Boolean hasAnswerImage;
    private final Binary answerImage;
    private final Boolean isMarked;

    public IndexCard(
        String id,
        String topicId,
        String question,
        String answer,
        Boolean hasAnswerImage,
        Binary answerImage,
        Boolean isMarked
    ) {
        this.id = id;
        this.topicId = topicId;
        this.question = question;
        this.answer = answer;
        this.hasAnswerImage = hasAnswerImage;
        this.answerImage = answerImage;
        this.isMarked = isMarked;
    }

    public String getId() {
        return id;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean hasAnswerImage() {
        return hasAnswerImage;
    }

    public Binary getAnswerImage() {
        return answerImage;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public IndexCardDto toDto() {
        return new IndexCardDto(id, topicId, question, answer, hasAnswerImage, isMarked);
    }

}
