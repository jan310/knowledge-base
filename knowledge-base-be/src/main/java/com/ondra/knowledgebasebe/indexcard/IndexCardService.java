package com.ondra.knowledgebasebe.indexcard;

import com.ondra.knowledgebasebe.exceptions.FileConversionException;
import com.ondra.knowledgebasebe.exceptions.IndexCardNotFoundException;
import com.ondra.knowledgebasebe.exceptions.TopicNotFoundException;
import com.ondra.knowledgebasebe.topic.TopicRepository;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndexCardService {

    private final IndexCardRepository indexCardRepository;
    private final TopicRepository topicRepository;

    public IndexCardService(IndexCardRepository indexCardRepository, TopicRepository topicRepository) {
        this.indexCardRepository = indexCardRepository;
        this.topicRepository = topicRepository;
    }

    public IndexCardDto addIndexCard(String topicId, String question, String answer, MultipartFile answerImage) {
        if (!topicRepository.existsById(topicId)) throw new TopicNotFoundException(topicId);

        boolean hasAnswerImage = (answerImage != null);

        Binary answerImageBinary;
        if (answerImage == null) answerImageBinary = null;
        else answerImageBinary = convertMultipartFileToBinary(answerImage);

        IndexCard indexCard = new IndexCard(null, topicId, question, answer, hasAnswerImage, answerImageBinary, false);
        return indexCardRepository.save(indexCard).toDto();
    }

    public List<IndexCardDto> getIndexCardsByTopicId(String topicId) {
        if (!topicRepository.existsById(topicId)) throw new TopicNotFoundException(topicId);
        return indexCardRepository.findAllByTopicIdAndExcludeAnswerImages(topicId)
            .stream().map(IndexCard::toDto).collect(Collectors.toList());
    }

    public byte[] getAnswerImage(String id) {
        IndexCard indexCard = indexCardRepository.findAnswerImageByIndexCardId(id).orElseThrow(() -> new IndexCardNotFoundException(id));
        return indexCard.getAnswerImage().getData();
    }

    public IndexCardDto setQuestion(String id, String question) {
        IndexCard indexCard = indexCardRepository.findById(id).orElseThrow(() -> new IndexCardNotFoundException(id));
        IndexCard updatedIndexCard = new IndexCard(
            id, indexCard.getTopicId(), question, indexCard.getAnswer(), indexCard.hasAnswerImage(), indexCard.getAnswerImage(), indexCard.isMarked()
        );
        return indexCardRepository.save(updatedIndexCard).toDto();
    }

    public IndexCardDto setAnswer(String id, String answer) {
        IndexCard indexCard = indexCardRepository.findById(id).orElseThrow(() -> new IndexCardNotFoundException(id));
        IndexCard updatedIndexCard = new IndexCard(
            id, indexCard.getTopicId(), indexCard.getQuestion(), answer, indexCard.hasAnswerImage(), indexCard.getAnswerImage(), indexCard.isMarked()
        );
        return indexCardRepository.save(updatedIndexCard).toDto();
    }

    public IndexCardDto setAnswerImage(String id, MultipartFile answerImage) {
        IndexCard indexCard = indexCardRepository.findById(id).orElseThrow(() -> new IndexCardNotFoundException(id));
        Binary answerImageBinary = convertMultipartFileToBinary(answerImage);
        IndexCard updatedIndexCard = new IndexCard(
            id, indexCard.getTopicId(), indexCard.getQuestion(), indexCard.getAnswer(), true, answerImageBinary, indexCard.isMarked()
        );
        return indexCardRepository.save(updatedIndexCard).toDto();
    }

    public IndexCardDto removeAnswerImage(String id) {
        IndexCard indexCard = indexCardRepository.findById(id).orElseThrow(() -> new IndexCardNotFoundException(id));
        IndexCard updatedIndexCard = new IndexCard(
            id, indexCard.getTopicId(), indexCard.getQuestion(), indexCard.getAnswer(), false, null, indexCard.isMarked()
        );
        return indexCardRepository.save(updatedIndexCard).toDto();
    }

    public IndexCardDto setMarked(String id, boolean isMarked) {
        IndexCard indexCard = indexCardRepository.findById(id).orElseThrow(() -> new IndexCardNotFoundException(id));
        IndexCard updatedIndexCard = new IndexCard(
            id, indexCard.getTopicId(), indexCard.getQuestion(), indexCard.getAnswer(), indexCard.hasAnswerImage(), indexCard.getAnswerImage(), isMarked
        );
        return indexCardRepository.save(updatedIndexCard).toDto();
    }

    public void deleteIndexCard(String id) {
        if (!indexCardRepository.existsById(id)) throw new IndexCardNotFoundException(id);
        indexCardRepository.deleteById(id);
    }

    public void deleteIndexCardsByTopicId(String topicId) {
        if (!topicRepository.existsById(topicId)) throw new TopicNotFoundException(topicId);
        indexCardRepository.deleteAllByTopicId(topicId);
    }

    private Binary convertMultipartFileToBinary(MultipartFile file) {
        try {
            return new Binary(file.getBytes());
        } catch (IOException ex) {
            throw new FileConversionException("Reading Bytes from DOCX-MultipartFile not possible");
        }
    }

}
