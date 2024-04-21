package com.ondra.knowledgebasebe.doc;

import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNameAlreadyTakenException;
import com.ondra.knowledgebasebe.exceptionhandling.exceptions.DocNotFoundException;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocService {
    private final DocRepository docRepository;
    private final FileConversionService fileConversionService;

    public DocService(DocRepository docRepository, FileConversionService fileConversionService) {
        this.docRepository = docRepository;
        this.fileConversionService = fileConversionService;
    }

    public DocDto addDoc(String userId, String name, MultipartFile docxFile) {
        if (docRepository.existsByUserIdAndName(userId, name)) throw new DocNameAlreadyTakenException(name, userId);
        Binary docxBinary = fileConversionService.convertMultipartFileToBinary(docxFile);
        Binary pdfBinary = fileConversionService.convertDocxMultipartFileToPdfBinary(docxFile);
        Doc doc = new Doc(null, userId, name, docxBinary, pdfBinary);
        return docRepository.save(doc).toDto();
    }

    public List<DocDto> getAllDocs(String userId) {
        return docRepository.findAllByUserIdAndExcludeBinaryData(userId).stream().map(Doc::toDto).collect(Collectors.toList());
    }

    public byte[] getPdf(String id, String userId) {
        Doc doc = docRepository.findPdfFileByDocIdAndUserId(id, userId).orElseThrow(() -> new DocNotFoundException(id, userId));
        return doc.getPdfFile().getData();
    }

    public byte[] getDocx(String id, String userId) {
        Doc doc = docRepository.findDocxFileByDocIdAndUserId(id, userId).orElseThrow(() -> new DocNotFoundException(id, userId));
        return doc.getDocxFile().getData();
    }

    public DocDto renameDoc(String id, String userId, String name) {
        Doc oldDoc = docRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new DocNotFoundException(id, userId));
        if (docRepository.existsByUserIdAndName(userId, name)) throw new DocNameAlreadyTakenException(name, userId);
        return docRepository.save(new Doc(id, userId, name, oldDoc.getDocxFile(), oldDoc.getPdfFile())).toDto();
    }

    public DocDto replaceFile(String id, String userId, MultipartFile docxFile) {
        Doc oldDoc = docRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new DocNotFoundException(id, userId));
        Binary docxBinary = fileConversionService.convertMultipartFileToBinary(docxFile);
        Binary pdfBinary = fileConversionService.convertDocxMultipartFileToPdfBinary(docxFile);
        Doc newDoc = new Doc(id, userId, oldDoc.getName(), docxBinary, pdfBinary);
        return docRepository.save(newDoc).toDto();
    }

    public void deleteDoc(String id, String userId) {
        if (!docRepository.existsByIdAndUserId(id, userId)) throw new DocNotFoundException(id, userId);
        docRepository.deleteByIdAndUserId(id, userId);
    }

}
