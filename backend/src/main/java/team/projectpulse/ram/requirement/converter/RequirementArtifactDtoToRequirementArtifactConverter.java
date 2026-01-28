package team.projectpulse.ram.requirement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.system.exception.ObjectNotFoundException;

@Component
public class RequirementArtifactDtoToRequirementArtifactConverter implements Converter<RequirementArtifactDto, RequirementArtifact> {

    private final DocumentSectionRepository documentSectionRepository;


    public RequirementArtifactDtoToRequirementArtifactConverter(DocumentSectionRepository documentSectionRepository) {
        this.documentSectionRepository = documentSectionRepository;
    }

    @Override
    public RequirementArtifact convert(RequirementArtifactDto source) {
        RequirementArtifact requirementArtifact = new RequirementArtifact();
        requirementArtifact.setId(source.id());
        requirementArtifact.setType(source.type());
        requirementArtifact.setArtifactKey(source.artifactKey());
        requirementArtifact.setTitle(source.title());
        requirementArtifact.setContent(source.content());
        requirementArtifact.setPriority(source.priority());
        requirementArtifact.setNotes(source.notes());
        requirementArtifact.setSourceDocumentSection(source.sourceSectionId() != null ?
                documentSectionRepository.findById(source.sourceSectionId()).orElseThrow(() -> new ObjectNotFoundException("document section", source.sourceSectionId())) : null);
        return requirementArtifact;
    }
}
