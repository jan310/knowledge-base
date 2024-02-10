package com.ondra.knowledgebasebe.indexcard;

public record IndexCardDto(
    String id,
    String topicId,
    String question,
    String answer,
    Boolean hasAnswerImage,
    Boolean isMarked
) {
}
