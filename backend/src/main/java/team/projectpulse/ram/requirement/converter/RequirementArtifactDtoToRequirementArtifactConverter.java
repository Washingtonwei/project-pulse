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
        // The id is not read from the payload: a create has the server assign it, and an update takes it from
        // the URL. Mapping it here would let a create carry an existing id, which turns save() into a merge over
        // that artifact and reassigns it to the caller's team.
        RequirementArtifact requirementArtifact = new RequirementArtifact();
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
