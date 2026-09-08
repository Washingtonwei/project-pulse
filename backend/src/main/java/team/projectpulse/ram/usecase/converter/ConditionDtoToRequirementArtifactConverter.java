package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.dto.ConditionDto;

@Component
public class ConditionDtoToRequirementArtifactConverter implements Converter<ConditionDto, RequirementArtifact> {
    @Override
    public RequirementArtifact convert(ConditionDto source) {
        // The id is not read from the payload. UseCase.preconditions and .postconditions cascade MERGE, so an
        // id here would let a use-case update rewrite any artifact in the database and reassign its team.
        RequirementArtifact conditionArtifact = new RequirementArtifact();
        conditionArtifact.setContent(source.condition());
        conditionArtifact.setType(source.type());
        conditionArtifact.setPriority(source.priority());
        conditionArtifact.setNotes(source.notes());
        return conditionArtifact;
    }
}
