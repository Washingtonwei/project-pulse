package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.dto.DocumentSectionDto;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.ram.requirement.converter.RequirementArtifactDtoToRequirementArtifactConverter;

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
        if (source.type() == SectionType.LIST) {
            documentSection.setRequirementArtifacts(
                    source.requirementArtifacts().stream()
                            .map(this.requirementArtifactDtoToRequirementArtifactConverter::convert)
                            .toList()
            );
        }
        documentSection.setGuidance(source.guidance());
        return documentSection;
    }
}
