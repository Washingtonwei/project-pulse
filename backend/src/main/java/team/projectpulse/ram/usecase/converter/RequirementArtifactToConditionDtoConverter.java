package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.dto.ConditionDto;
import team.projectpulse.ram.usecase.dto.OtherUseCaseDto;

import java.util.List;

@Component
public class RequirementArtifactToConditionDtoConverter implements Converter<RequirementArtifact, ConditionDto> {

    /**
     * Converts a condition with no other use cases listed. Prefer {@link #convert(RequirementArtifact, List)}, which
     * carries the other use cases that the use case editor lists as "Also used by" (extension 6b1).
     */
    @Override
    public ConditionDto convert(RequirementArtifact source) {
        return convert(source, List.of());
    }

    public ConditionDto convert(RequirementArtifact source, List<OtherUseCaseDto> otherUseCases) {
        return new ConditionDto(
                source.getId(),
                source.getArtifactKey(),
                source.getContent(),
                source.getType(),
                source.getPriority(),
                source.getNotes(),
                otherUseCases
        );
    }
}
