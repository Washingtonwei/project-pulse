package team.projectpulse.ram.document;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.document.converter.DocumentSectionDtoToDocumentSectionConverter;
import team.projectpulse.ram.document.converter.DocumentSectionLockToDocumentSectionLockDtoConverter;
import team.projectpulse.ram.document.converter.DocumentSectionToDocumentSectionDtoConverter;
import team.projectpulse.ram.document.dto.DocumentSectionDto;
import team.projectpulse.ram.document.dto.DocumentSectionLockDto;
import team.projectpulse.ram.document.dto.DocumentSectionLockRequest;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class DocumentSectionController {

    private final DocumentSectionService documentSectionService;
    private final DocumentSectionLockToDocumentSectionLockDtoConverter documentSectionLockToDocumentSectionLockDtoConverter;
    private final DocumentSectionToDocumentSectionDtoConverter documentSectionToDocumentSectionDtoConverter;
    private final DocumentSectionDtoToDocumentSectionConverter documentSectionDtoToDocumentSectionConverter;


    public DocumentSectionController(DocumentSectionService documentSectionService, DocumentSectionLockToDocumentSectionLockDtoConverter documentSectionLockToDocumentSectionLockDtoConverter, DocumentSectionToDocumentSectionDtoConverter documentSectionToDocumentSectionDtoConverter, DocumentSectionDtoToDocumentSectionConverter documentSectionDtoToDocumentSectionConverter) {
        this.documentSectionService = documentSectionService;
        this.documentSectionLockToDocumentSectionLockDtoConverter = documentSectionLockToDocumentSectionLockDtoConverter;
        this.documentSectionDtoToDocumentSectionConverter = documentSectionDtoToDocumentSectionConverter;
        this.documentSectionToDocumentSectionDtoConverter = documentSectionToDocumentSectionDtoConverter;
    }

    @PutMapping("/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}")
    public Result updateDocumentSectionContent(@PathVariable Integer teamId, @PathVariable Long documentId, @PathVariable Long documentSectionId, @RequestBody DocumentSectionDto documentSectionDto) {
        DocumentSection update = this.documentSectionDtoToDocumentSectionConverter.convert(documentSectionDto);
        DocumentSection updatedDocumentSection = this.documentSectionService.updateDocumentSectionContent(teamId, documentId, documentSectionId, update);
        DocumentSectionDto updatedDocumentSectionDto = this.documentSectionToDocumentSectionDtoConverter.convert(updatedDocumentSection);
        return new Result(true, 200, "Update document section content successfully", updatedDocumentSectionDto);
    }

    @GetMapping("/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/lock")
    public Result getDocumentSectionLockStatus(@PathVariable Integer teamId, @PathVariable Long documentId, @PathVariable Long documentSectionId) {
        DocumentSectionLock lock = this.documentSectionService.findSectionLock(teamId, documentId, documentSectionId);
        DocumentSectionLockDto lockDto = this.documentSectionLockToDocumentSectionLockDtoConverter.convert(lock);
        return new Result(true, StatusCode.SUCCESS, "Find lock status successfully", lockDto);
    }

    @PutMapping("/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/lock")
    public Result lockDocumentSection(@PathVariable Integer teamId, @PathVariable Long documentId, @PathVariable Long documentSectionId, @RequestBody DocumentSectionLockRequest lockRequest) {
        String reason = lockRequest != null ? lockRequest.reason() : null;
        DocumentSectionLock lock = this.documentSectionService.lockSection(teamId, documentId, documentSectionId, reason);
        DocumentSectionLockDto lockDto = this.documentSectionLockToDocumentSectionLockDtoConverter.convert(lock);
        return new Result(true, 200, "Lock document section successfully", lockDto);
    }

    @DeleteMapping("/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/lock")
    public Result unlockDocumentSection(@PathVariable Integer teamId, @PathVariable Long documentId, @PathVariable Long documentSectionId) {
        this.documentSectionService.unlockSection(teamId, documentId, documentSectionId);
        return new Result(true, 200, "Unlock document section successfully");
    }

}
