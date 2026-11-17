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
        // The id is mapped as a pairing hint only: the document section save pairs it with the section's own rows
        // (DocumentSectionService.resolveSectionItems). It never selects the row to write. A create clears it in
        // RequirementArtifactService.saveRequirementArtifact, so it can never merge over the artifact it names, and an
        // update takes the artifact from the URL and copies fields onto the stored row (OI-46).
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
