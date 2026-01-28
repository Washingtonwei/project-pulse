package team.projectpulse.ram.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.document.converter.RequirementDocumentDtoToRequirementDocumentConverter;
import team.projectpulse.ram.document.converter.RequirementDocumentToRequirementDocumentDtoConverter;
import team.projectpulse.ram.document.converter.RequirementDocumentToRequirementDocumentSummaryDtoConverter;
import team.projectpulse.ram.document.dto.CreateRequirementDocumentRequest;
import team.projectpulse.ram.document.dto.RequirementDocumentDto;
import team.projectpulse.ram.document.dto.RequirementDocumentSummaryDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class DocumentController {

    private final DocumentService documentService;
    private final RequirementDocumentToRequirementDocumentDtoConverter requirementDocumentToRequirementDocumentDtoConverter;
    private final RequirementDocumentDtoToRequirementDocumentConverter requirementDocumentDtoToRequirementDocumentConverter;
    private final RequirementDocumentToRequirementDocumentSummaryDtoConverter requirementDocumentToRequirementDocumentSummaryDtoConverter;


    public DocumentController(DocumentService documentService, RequirementDocumentToRequirementDocumentDtoConverter requirementDocumentToRequirementDocumentDtoConverter, RequirementDocumentDtoToRequirementDocumentConverter requirementDocumentDtoToRequirementDocumentConverter, RequirementDocumentToRequirementDocumentSummaryDtoConverter requirementDocumentToRequirementDocumentSummaryDtoConverter) {
        this.documentService = documentService;
        this.requirementDocumentToRequirementDocumentDtoConverter = requirementDocumentToRequirementDocumentDtoConverter;
        this.requirementDocumentDtoToRequirementDocumentConverter = requirementDocumentDtoToRequirementDocumentConverter;
        this.requirementDocumentToRequirementDocumentSummaryDtoConverter = requirementDocumentToRequirementDocumentSummaryDtoConverter;
    }

    @PostMapping("/teams/{teamId}/documents/search")
    public Result findDocumentsByCriteria(@PathVariable Integer teamId, @RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<RequirementDocument> documentPage = this.documentService.findByCriteria(teamId, searchCriteria, pageable);
        Page<RequirementDocumentSummaryDto> documentSummaryDtoPage = documentPage.map(this.requirementDocumentToRequirementDocumentSummaryDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find requirement documents successfully", documentSummaryDtoPage);
    }

    @GetMapping("/teams/{teamId}/documents/{documentId}")
    public Result findDocumentById(@PathVariable Integer teamId, @PathVariable Long documentId) {
        RequirementDocument document = this.documentService.findDocumentByIdWithFullGraph(teamId, documentId);
        RequirementDocumentDto documentDto = this.requirementDocumentToRequirementDocumentDtoConverter.convert(document);
        return new Result(true, StatusCode.SUCCESS, "Find requirement document successfully", documentDto);
    }

    @PostMapping("/teams/{teamId}/documents")
    public Result createDocument(@PathVariable Integer teamId, @RequestBody CreateRequirementDocumentRequest request) {
        RequirementDocument createdDocument = this.documentService.createRequirementDocument(teamId, request.type());
        RequirementDocumentDto documentDto = this.requirementDocumentToRequirementDocumentDtoConverter.convert(createdDocument);
        return new Result(true, StatusCode.SUCCESS, "Create requirement document successfully", documentDto);
    }

    @PatchMapping("/teams/{teamId}/documents/{documentId}")
    public Result updateDocumentStatus(@PathVariable Integer teamId, @PathVariable Long documentId, @RequestBody RequirementDocumentDto documentDto) {
        RequirementDocument update = this.requirementDocumentDtoToRequirementDocumentConverter.convert(documentDto);
        this.documentService.updateRequirementDocument(teamId, documentId, update);
        return new Result(true, StatusCode.SUCCESS, "Update requirement document status successfully", null);
    }

}
