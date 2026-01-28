package team.projectpulse.ram.requirement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.dto.RequirementArtifactSummaryDto;

@Component
public class RequirementArtifactToRequirementArtifactSummaryDtoConverter implements Converter<RequirementArtifact, RequirementArtifactSummaryDto> {
    @Override
    public RequirementArtifactSummaryDto convert(RequirementArtifact source) {
        return new RequirementArtifactSummaryDto(
                source.getId(),
                source.getArtifactKey(),
                source.getTitle(),
                source.getContent(),
                source.getType(),
                source.getPriority()
        );
    }
}
