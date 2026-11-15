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

    private final UseCaseMainStepDtoToUseCaseMainStepConverter useCaseMainStepDtoToUseCaseMainStepConverter;

    public UseCaseDtoToUseCaseConverter(UseCaseMainStepDtoToUseCaseMainStepConverter useCaseMainStepDtoToUseCaseMainStepConverter) {
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
        // from the route. It sets no actor and no condition: the controller passes those to the service as
        // UseCaseReferences, and the service resolves them through team-scoped lookups.
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

        useCase.replaceMainSteps(source.mainSteps().stream()
                .map(this.useCaseMainStepDtoToUseCaseMainStepConverter::convert)
                .collect(Collectors.toList()));

        return useCase;
    }
}
