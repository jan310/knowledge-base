package com.ondra.knowledgebasebe.doc;

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
@RequestMapping("/doc-api/v1")
public class DocController {

    private final DocValidator docValidator;
    private final DocService docService;

    public DocController(DocValidator docValidator, DocService docService) {
        this.docValidator = docValidator;
        this.docService = docService;
    }

    @PostMapping(value = "/doc")
    @ResponseStatus(CREATED)
    public DocDto addDoc(@RequestParam String topicId, @RequestParam String name, @RequestParam MultipartFile docxFile) {
        docValidator.validateIdAndNameAndDocxFile(topicId, name, docxFile);
        return docService.addDoc(topicId, name, docxFile);
    }

    @GetMapping(value = "/docs")
    @ResponseStatus(OK)
    public List<DocDto> getAllDocs() {
        return docService.getAllDocs();
    }

    @GetMapping(value = "/doc/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseStatus(OK)
    public byte[] getPdf(@RequestParam String id) {
        docValidator.validateId(id);
        return docService.getPdf(id);
    }

    @GetMapping(value = "/doc/docx", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @ResponseStatus(OK)
    public byte[] getDocx(@RequestParam String id) {
        docValidator.validateId(id);
        return docService.getDocx(id);
    }

    @PutMapping("/doc/name")
    @ResponseStatus(ACCEPTED)
    public DocDto renameDoc(@RequestParam String id, @RequestParam String name) {
        docValidator.validateIdAndName(id, name);
        return docService.renameDoc(id, name);
    }

    @PutMapping("/doc/docx")
    @ResponseStatus(ACCEPTED)
    public DocDto replaceFile(@RequestParam String id, @RequestParam MultipartFile docxFile) {
        docValidator.validateIdAndDocxFile(id, docxFile);
        return docService.replaceFile(id, docxFile);
    }

    @DeleteMapping(value = "/doc")
    @ResponseStatus(NO_CONTENT)
    public void deleteDoc(@RequestParam String id) {
        docValidator.validateId(id);
        docService.deleteDoc(id);
    }

    @DeleteMapping(value = "/docs/by-topicId")
    @ResponseStatus(NO_CONTENT)
    public void deleteDocsByTopicId(@RequestParam String topicId) {
        docValidator.validateId(topicId);
        docService.deleteDocsByTopicId(topicId);
    }

}
