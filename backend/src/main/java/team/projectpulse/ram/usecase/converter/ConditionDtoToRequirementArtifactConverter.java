package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.dto.ConditionDto;

@Component
public class ConditionDtoToRequirementArtifactConverter implements Converter<ConditionDto, RequirementArtifact> {
    @Override
    public RequirementArtifact convert(ConditionDto source) {
        RequirementArtifact conditionArtifact = new RequirementArtifact();
        conditionArtifact.setId(source.id());
        conditionArtifact.setContent(source.condition());
        conditionArtifact.setType(source.type());
        conditionArtifact.setPriority(source.priority());
        conditionArtifact.setNotes(source.notes());
        return conditionArtifact;
    }
}
