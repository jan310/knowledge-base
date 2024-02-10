package com.ondra.knowledgebasebe.topic;

import com.ondra.knowledgebasebe.doc.DocRepository;
import com.ondra.knowledgebasebe.exceptions.ConstraintViolationException;
import com.ondra.knowledgebasebe.exceptions.TopicNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptions.TopicNotFoundException;
import com.ondra.knowledgebasebe.indexcard.IndexCardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final DocRepository docRepository;
    private final IndexCardRepository indexCardRepository;

    public TopicService(TopicRepository topicRepository, DocRepository docRepository, IndexCardRepository indexCardRepository) {
        this.topicRepository = topicRepository;
        this.docRepository = docRepository;
        this.indexCardRepository = indexCardRepository;
    }

    public TopicDto addTopic(String name) {
        if (topicRepository.existsByName(name)) throw new TopicNameAlreadyTakenException(name);
        return topicRepository.save(new Topic(null, name)).toDto();
    }

    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream().map(Topic::toDto).collect(Collectors.toList());
    }

    public TopicDto renameTopic(String id, String name) {
        if (!topicRepository.existsById(id)) throw new TopicNotFoundException(id);
        if (topicRepository.existsByName(name)) throw new TopicNameAlreadyTakenException(name);
        return topicRepository.save(new Topic(id, name)).toDto();
    }

    public void deleteTopic(String id) {
        if (!topicRepository.existsById(id)) throw new TopicNotFoundException(id);
        if (docRepository.existsByTopicId(id))
            throw new ConstraintViolationException("Topic with ID " + id + " cannot be deleted as long as documentations exist that are linked to this topic");
        if (indexCardRepository.existsByTopicId(id))
            throw new ConstraintViolationException("Topic with ID " + id + " cannot be deleted as long as index cards exist that are linked to this topic");
        topicRepository.deleteById(id);
    }

}
