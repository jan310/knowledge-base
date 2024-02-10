package com.ondra.knowledgebasebe.indexcard;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/index-card-api/v1")
public class IndexCardController {

    private final IndexCardValidator indexCardValidator;
    private final IndexCardService indexCardService;

    public IndexCardController(IndexCardValidator indexCardValidator, IndexCardService indexCardService) {
        this.indexCardValidator = indexCardValidator;
        this.indexCardService = indexCardService;
    }

    @PostMapping(value = "/index-card")
    @ResponseStatus(CREATED)
    public IndexCardDto addIndexCard(
        @RequestParam String topicId,
        @RequestParam String question,
        @RequestParam String answer,
        @RequestParam(required = false) MultipartFile answerImage
    ) {
        indexCardValidator.validateIdAndQuestionAndAnswerAndAnswerImage(topicId, question, answer, answerImage);
        return indexCardService.addIndexCard(topicId, question, answer, answerImage);
    }

    @GetMapping(value = "/index-cards")
    @ResponseStatus(OK)
    public List<IndexCardDto> getIndexCardsByTopicId(@RequestParam String topicId) {
        indexCardValidator.validateId(topicId);
        return indexCardService.getIndexCardsByTopicId(topicId);
    }

    @GetMapping(value = "/index-card", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseStatus(OK)
    public byte[] getAnswerImage(@RequestParam String id) {
        indexCardValidator.validateId(id);
        return indexCardService.getAnswerImage(id);
    }

    @PutMapping(value = "/index-card/question")
    @ResponseStatus(ACCEPTED)
    public IndexCardDto setQuestion(@RequestParam String id, @RequestParam String question) {
        indexCardValidator.validateIdAndQuestion(id, question);
        return indexCardService.setQuestion(id, question);
    }

    @PutMapping(value = "/index-card/answer")
    @ResponseStatus(ACCEPTED)
    public IndexCardDto setAnswer(@RequestParam String id, @RequestParam String answer) {
        indexCardValidator.validateIdAndAnswer(id, answer);
        return indexCardService.setAnswer(id, answer);
    }

    @PutMapping(value = "/index-card/answer-image")
    @ResponseStatus(ACCEPTED)
    public IndexCardDto setAnswerImage(@RequestParam String id, @RequestParam MultipartFile answerImage) {
        indexCardValidator.validateIdAndAnswerImage(id, answerImage);
        return indexCardService.setAnswerImage(id, answerImage);
    }

    @PutMapping(value = "/index-card/remove/answer-image")
    @ResponseStatus(ACCEPTED)
    public IndexCardDto removeAnswerImage(@RequestParam String id) {
        indexCardValidator.validateId(id);
        return indexCardService.removeAnswerImage(id);
    }

    @PutMapping(value = "/index-card/marked")
    @ResponseStatus(ACCEPTED)
    public IndexCardDto setMarked(@RequestParam String id, @RequestParam boolean isMarked) {
        indexCardValidator.validateId(id);
        return indexCardService.setMarked(id, isMarked);
    }

    @DeleteMapping(value = "/index-card")
    @ResponseStatus(NO_CONTENT)
    public void deleteIndexCard(@RequestParam String id) {
        indexCardValidator.validateId(id);
        indexCardService.deleteIndexCard(id);
    }

    @DeleteMapping(value = "/index-cards/by-topicId")
    @ResponseStatus(NO_CONTENT)
    public void deleteIndexCardsByTopicId(@RequestParam String topicId) {
        indexCardValidator.validateId(topicId);
        indexCardService.deleteIndexCardsByTopicId(topicId);
    }

}
