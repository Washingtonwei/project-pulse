package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.dto.DocumentSectionDto;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.converter.RequirementArtifactDtoToRequirementArtifactConverter;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentSectionDtoToDocumentSectionConverter implements Converter<DocumentSectionDto, DocumentSection> {

    private final RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter;

    public DocumentSectionDtoToDocumentSectionConverter(RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter) {
        this.requirementArtifactDtoToRequirementArtifactConverter = requirementArtifactDtoToRequirementArtifactConverter;
    }

    @Override
    public DocumentSection convert(DocumentSectionDto source) {
        DocumentSection documentSection = new DocumentSection();
        documentSection.setId(source.id());
        documentSection.setSectionKey(source.sectionKey());
        documentSection.setTitle(source.title());
        documentSection.setType(source.type());
        documentSection.setContent(source.content());
        documentSection.setGuidance(source.guidance());
        // The list's items, in display order, as request data for DocumentSectionService.resolveSectionItems, their
        // only reader. Each keeps the body's id as a pairing hint, looked up only among the document section's own
        // rows. They are not rows and must never be saved or attached (see updateDocumentSectionContent). A missing
        // list is an empty one.
        List<RequirementArtifact> items = new ArrayList<>();
        if (source.requirementArtifacts() != null) {
            source.requirementArtifacts().forEach(item -> items.add(this.requirementArtifactDtoToRequirementArtifactConverter.convert(item)));
        }
        documentSection.setRequirementArtifacts(items);
        return documentSection;
    }
}
