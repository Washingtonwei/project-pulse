package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.usecase.UseCase;
import team.projectpulse.ram.usecase.dto.UseCaseDto;

import java.util.stream.Collectors;

@Component
public class UseCaseDtoToUseCaseConverter implements Converter<UseCaseDto, UseCase> {

    private final ConditionDtoToRequirementArtifactConverter conditionDtoToRequirementArtifactConverter;
    private final UseCaseMainStepDtoToUseCaseMainStepConverter useCaseMainStepDtoToUseCaseMainStepConverter;

    public UseCaseDtoToUseCaseConverter(ConditionDtoToRequirementArtifactConverter conditionDtoToRequirementArtifactConverter, UseCaseMainStepDtoToUseCaseMainStepConverter useCaseMainStepDtoToUseCaseMainStepConverter) {
        this.conditionDtoToRequirementArtifactConverter = conditionDtoToRequirementArtifactConverter;
        this.useCaseMainStepDtoToUseCaseMainStepConverter = useCaseMainStepDtoToUseCaseMainStepConverter;
    }

    @Override
    public UseCase convert(UseCaseDto source) {
        // The id is not read from the payload: a create has the server assign it, and an update takes it from
        // the URL. UseCase.artifact is @OneToOne(cascade = ALL) @MapsId, so the two share an id, and mapping it
        // here would let a create merge over the artifact that id names and reassign it to the caller's team.
        //
        // A converter has no path variables, so it is the wrong layer to resolve any reference at, and this one
        // resolves none (OI-46). It reads no team: the body's teamId is ignored, and UseCaseService takes the team
        // from the route. It sets no actor: the controller passes the actor ids to the service as UseCaseActorIds,
        // and the service resolves them through a team-scoped lookup.
        UseCase useCase = new UseCase();
        useCase.setUseCaseTrigger(source.trigger());

        RequirementArtifact useCaseArtifact = new RequirementArtifact();
        useCaseArtifact.setType(RequirementArtifactType.USE_CASE);
        // No artifact key: it used to be built from the payload id, and the real one is minted server-side by
        // RequirementArtifactService.saveRequirementArtifact.
        useCaseArtifact.setTitle(source.title());
        useCaseArtifact.setContent(source.description());
        useCaseArtifact.setPriority(source.priority());
        useCaseArtifact.setNotes(source.notes());
        useCase.setArtifact(useCaseArtifact);

        useCase.replacePreconditions(source.preconditions().stream()
                .map(this.conditionDtoToRequirementArtifactConverter::convert)
                .collect(Collectors.toSet()));
        useCase.replacePostconditions(source.postconditions().stream()
                .map(this.conditionDtoToRequirementArtifactConverter::convert)
                .collect(Collectors.toSet()));

        useCase.replaceMainSteps(source.mainSteps().stream()
                .map(this.useCaseMainStepDtoToUseCaseMainStepConverter::convert)
                .collect(Collectors.toList()));

        return useCase;
    }
}
