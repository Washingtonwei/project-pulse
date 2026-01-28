package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.usecase.UseCase;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UseCaseDtoToUseCaseConverter implements Converter<UseCaseDto, UseCase> {

    private final RequirementArtifactRepository requirementArtifactRepository;
    private final TeamRepository teamRepository;
    private final ConditionDtoToRequirementArtifactConverter conditionDtoToRequirementArtifactConverter;
    private final UseCaseMainStepDtoToUseCaseMainStepConverter useCaseMainStepDtoToUseCaseMainStepConverter;

    public UseCaseDtoToUseCaseConverter(RequirementArtifactRepository requirementArtifactRepository, TeamRepository teamRepository, ConditionDtoToRequirementArtifactConverter conditionDtoToRequirementArtifactConverter, UseCaseMainStepDtoToUseCaseMainStepConverter useCaseMainStepDtoToUseCaseMainStepConverter) {
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.teamRepository = teamRepository;
        this.conditionDtoToRequirementArtifactConverter = conditionDtoToRequirementArtifactConverter;
        this.useCaseMainStepDtoToUseCaseMainStepConverter = useCaseMainStepDtoToUseCaseMainStepConverter;
    }

    @Override
    public UseCase convert(UseCaseDto source) {
        UseCase useCase = new UseCase();
        useCase.setId(source.id());
        useCase.setUseCaseTrigger(source.trigger());

        RequirementArtifact useCaseArtifact = new RequirementArtifact();

        useCaseArtifact.setId(source.id());
        Team team = this.teamRepository.findById(source.teamId()).orElseThrow(() -> new ObjectNotFoundException("team", source.teamId()));
        useCaseArtifact.setTeam(team);
        useCaseArtifact.setType(RequirementArtifactType.USE_CASE);
        useCaseArtifact.setArtifactKey("UC-" + source.id());
        useCaseArtifact.setTitle(source.title());
        useCaseArtifact.setContent(source.description());
        useCaseArtifact.setPriority(source.priority());
        useCaseArtifact.setNotes(source.notes());
        useCase.setArtifact(useCaseArtifact);

        useCase.setPrimaryActor(this.requirementArtifactRepository.findById(source.primaryActorId()).orElseThrow(() -> new ObjectNotFoundException("primary actor", source.primaryActorId())));
        Set<RequirementArtifact> secondaryActors = source.secondaryActorIds().stream().map(id -> this.requirementArtifactRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("secondary actor", id))).collect(Collectors.toSet());
        useCase.replaceSecondaryActors(secondaryActors);

        useCase.replacePreconditions(source.preconditions().stream().map(
                precondition -> {
                    RequirementArtifact preconditionArtifact = this.conditionDtoToRequirementArtifactConverter.convert(precondition);
                    preconditionArtifact.setTeam(team);
                    return preconditionArtifact;
                }
        ).collect(Collectors.toSet()));

        useCase.replacePostconditions(source.postconditions().stream().map(
                postcondition -> {
                    RequirementArtifact postconditionArtifact = this.conditionDtoToRequirementArtifactConverter.convert(postcondition);
                    postconditionArtifact.setTeam(team);
                    return postconditionArtifact;
                }
        ).collect(Collectors.toSet()));

        useCase.replaceMainSteps(source.mainSteps().stream()
                .map(this.useCaseMainStepDtoToUseCaseMainStepConverter::convert)
                .collect(Collectors.toList()));

        return useCase;
    }
}
