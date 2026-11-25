package team.projectpulse.ram.requirement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;

@Component
public class RequirementArtifactDtoToRequirementArtifactConverter implements Converter<RequirementArtifactDto, RequirementArtifact> {

    /**
     * Maps only the fields a caller may write: type, artifact key, title, content, priority and notes. The key is the
     * student's choice (UC-ART-create-artifact, "Artifact key rules"); RequirementArtifactService checks it on a create
     * and treats a different one on an update as a rename, so mapping it here trusts nothing. The id, home and version
     * are the server's: a create takes its home from the type (BR-artifact-home), and an update takes the artifact from
     * the URL and its version from the request separately (OI-46).
     */
    @Override
    public RequirementArtifact convert(RequirementArtifactDto source) {
        RequirementArtifact requirementArtifact = new RequirementArtifact();
        requirementArtifact.setType(source.type());
        requirementArtifact.setArtifactKey(source.artifactKey());
        requirementArtifact.setTitle(source.title());
        requirementArtifact.setContent(source.content());
        requirementArtifact.setPriority(source.priority());
        requirementArtifact.setNotes(source.notes());
        return requirementArtifact;
    }
}
