package com.ondra.knowledgebasebe.doc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@RestController
@RequestMapping("/api/v1/docs")
@CrossOrigin(origins = "http://localhost:5173/")
public class DocController {

    private final DocValidator docValidator;
    private final DocService docService;

    public DocController(DocValidator docValidator, DocService docService) {
        this.docValidator = docValidator;
        this.docService = docService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public DocDto addDoc(
        @RequestHeader("Authorization") String bearerToken,
        @RequestParam String name,
        @RequestParam MultipartFile docxFile
    ) {
        docValidator.validateName(name);
        docValidator.validateDocxFile(docxFile);
        String userId = getUserIdFromBearerToken(bearerToken);
        return docService.addDoc(userId, name, docxFile);
    }

    @GetMapping
    @ResponseStatus(OK)
    public List<DocDto> getAllDocs(
        @RequestHeader("Authorization") String bearerToken
    ) {
        String userId = getUserIdFromBearerToken(bearerToken);
        return docService.getAllDocs(userId);
    }

    @GetMapping(value = "/{id}/pdf", produces = APPLICATION_PDF_VALUE)
    @ResponseStatus(OK)
    public byte[] getPdf(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable String id
    ) {
        docValidator.validateId(id);
        String userId = getUserIdFromBearerToken(bearerToken);
        return docService.getPdf(id, userId);
    }

    @GetMapping(value = "/{id}/docx", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @ResponseStatus(OK)
    public byte[] getDocx(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable String id
    ) {
        docValidator.validateId(id);
        String userId = getUserIdFromBearerToken(bearerToken);
        return docService.getDocx(id, userId);
    }

    @PatchMapping("/{id}/rename")
    @ResponseStatus(ACCEPTED)
    public DocDto renameDoc(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable String id,
        @RequestParam String name
    ) {
        docValidator.validateId(id);
        docValidator.validateName(name);
        String userId = getUserIdFromBearerToken(bearerToken);
        return docService.renameDoc(id, userId, name);
    }

    @PatchMapping("/{id}/replace-file")
    @ResponseStatus(ACCEPTED)
    public DocDto replaceFile(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable String id,
        @RequestParam MultipartFile docxFile
    ) {
        docValidator.validateId(id);
        docValidator.validateDocxFile(docxFile);
        String userId = getUserIdFromBearerToken(bearerToken);
        return docService.replaceFile(id, userId, docxFile);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteDoc(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable String id
    ) {
        docValidator.validateId(id);
        String userId = getUserIdFromBearerToken(bearerToken);
        docService.deleteDoc(id, userId);
    }

    private String getUserIdFromBearerToken(String bearerToken) {
        String jwt = bearerToken.replace("Bearer ", "");
        String jwtPayload = jwt.split("\\.")[1];
        String decodedJwtPayload = new String(Base64.getDecoder().decode(jwtPayload));
        JsonNode jsonNode;
        try {
            jsonNode = new ObjectMapper().readTree(decodedJwtPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonNode.path("sub").asText();
    }

}
