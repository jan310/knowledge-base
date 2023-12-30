package com.ondra.knowledgebasebe.topic;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/topic-api/v1")
public class TopicController {

    private final TopicValidator topicValidator;
    private final TopicService topicService;

    public TopicController(TopicValidator topicValidator, TopicService topicService) {
        this.topicValidator = topicValidator;
        this.topicService = topicService;
    }

    @PostMapping("/topic")
    @ResponseStatus(CREATED)
    public TopicDto addTopic(@RequestParam String name) {
        topicValidator.validateName(name);
        return topicService.addTopic(name);
    }

    @GetMapping("/topics")
    @ResponseStatus(OK)
    public List<TopicDto> getAllTopics() {
        return topicService.getAllTopics();
    }

    @PutMapping("/topic")
    @ResponseStatus(ACCEPTED)
    public TopicDto renameTopic(@RequestParam String id, @RequestParam String name) {
        topicValidator.validateIdAndName(id, name);
        return topicService.renameTopic(id, name);
    }

    @DeleteMapping("/topic")
    @ResponseStatus(NO_CONTENT)
    public void deleteTopic(@RequestParam String id) {
        topicValidator.validateId(id);
        topicService.deleteTopic(id);
    }

}
