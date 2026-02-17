package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.UseCase;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

import java.util.stream.Collectors;

@Component
public class UseCaseToUseCaseDtoConverter implements Converter<UseCase, UseCaseDto> {

    private final UseCaseMainStepToUseCaseMainStepDtoConverter useCaseMainStepToUseCaseMainStepDtoConverter;
    private final RequirementArtifactToConditionDtoConverter requirementArtifactToConditionDtoConverter;
    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;
    private final UseCaseLockToUseCaseLockDtoConverter useCaseLockToUseCaseLockDtoConverter;

    public UseCaseToUseCaseDtoConverter(UseCaseMainStepToUseCaseMainStepDtoConverter useCaseMainStepToUseCaseMainStepDtoConverter, RequirementArtifactToConditionDtoConverter requirementArtifactToConditionDtoConverter, PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter, UseCaseLockToUseCaseLockDtoConverter useCaseLockToUseCaseLockDtoConverter) {
        this.useCaseMainStepToUseCaseMainStepDtoConverter = useCaseMainStepToUseCaseMainStepDtoConverter;
        this.requirementArtifactToConditionDtoConverter = requirementArtifactToConditionDtoConverter;
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
        this.useCaseLockToUseCaseLockDtoConverter = useCaseLockToUseCaseLockDtoConverter;
    }

    @Override
    public UseCaseDto convert(UseCase source) {
        return new UseCaseDto(
                source.getId(),
                source.getArtifact().getArtifactKey(),
                source.getArtifact().getTitle(),
                source.getArtifact().getContent(),
                source.getArtifact().getTeam().getTeamId(),
                source.getPrimaryActor().getId(),
                source.getSecondaryActors().stream().map(RequirementArtifact::getId).collect(Collectors.toSet()),
                source.getUseCaseTrigger(),
                source.getPreconditions().stream().map(requirementArtifactToConditionDtoConverter::convert).collect(Collectors.toSet()),
                source.getPostconditions().stream().map(requirementArtifactToConditionDtoConverter::convert).collect(Collectors.toSet()),
                source.getMainSteps().stream().map(useCaseMainStepToUseCaseMainStepDtoConverter::convert).collect(Collectors.toList()),
                source.getArtifact().getPriority(),
                source.getArtifact().getNotes(),
                source.getArtifact().getCreatedAt(),
                source.getArtifact().getUpdatedAt(),
                source.getArtifact().getCreatedBy() == null ? null : peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getArtifact().getCreatedBy()),
                source.getArtifact().getUpdatedBy() == null ? null : peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getArtifact().getUpdatedBy()),
                source.getVersion(),
                source.getLock() == null ? null : useCaseLockToUseCaseLockDtoConverter.convert(source.getLock())
        );
    }

}
