package com.ondra.knowledgebasebe.indexcard;

import com.ondra.knowledgebasebe.exceptions.FileConversionException;
import com.ondra.knowledgebasebe.exceptions.InvalidArgumentException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class IndexCardValidator {

    public void validateId(String id) {
        if (id == null) throw new InvalidArgumentException("ID cannot be null");
    }

    public void validateIdAndQuestionAndAnswerAndAnswerImage(String id, String question, String answer, MultipartFile answerImage) {
        validateId(id);
        validateQuestion(question);
        validateAnswer(answer);
        validateAnswerImage(answerImage);
    }

    public void validateIdAndQuestion(String id, String question) {
        validateId(id);
        validateQuestion(question);
    }

    public void validateIdAndAnswer(String id, String answer) {
        validateId(id);
        validateAnswer(answer);
    }

    public void validateIdAndAnswerImage(String id, MultipartFile answerImage) {
        validateId(id);
        validateAnswerImage(answerImage);
    }

    private void validateQuestion(String question) {
        if (question == null) throw new InvalidArgumentException("Question cannot be null");
        if (question.isEmpty()) throw new InvalidArgumentException("Question cannot be empty");
        if (question.length() > 200) throw new InvalidArgumentException("Question cannot be longer than 200 characters");
    }

    private void validateAnswer(String answer) {
        if (answer == null) throw new InvalidArgumentException("Answer cannot be null");
        if (answer.isEmpty()) throw new InvalidArgumentException("Answer cannot be empty");
        if (answer.length() > 1000) throw new InvalidArgumentException("Answer cannot be longer than 1000 characters");
    }

    private void validateAnswerImage(MultipartFile answerImage) {
        if (answerImage != null) {
            if (answerImage.getContentType() == null || !answerImage.getContentType().equals(MediaType.IMAGE_PNG_VALUE))
                throw new InvalidArgumentException("File has to be a PNG-file");
            long fileSize;
            try {
                fileSize = answerImage.getBytes().length;
            } catch (IOException ex) {
                throw new FileConversionException("Reading Bytes from MultipartFile not possible");
            }
            if (fileSize > 1000000) throw new InvalidArgumentException("File cannot be larger than 1 MB");
        }
    }

}
