package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.dto.ConditionDto;

@Component
public class RequirementArtifactToConditionDtoConverter implements Converter<RequirementArtifact, ConditionDto> {
    @Override
    public ConditionDto convert(RequirementArtifact source) {
        return new ConditionDto(
                source.getId(),
                source.getContent(),
                source.getType(),
                source.getPriority(),
                source.getNotes()
        );
    }
}
