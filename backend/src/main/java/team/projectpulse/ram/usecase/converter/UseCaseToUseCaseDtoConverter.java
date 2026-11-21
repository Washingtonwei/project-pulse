package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.ConditionUse;
import team.projectpulse.ram.usecase.UseCase;
import team.projectpulse.ram.usecase.dto.ConditionDto;
import team.projectpulse.ram.usecase.dto.OtherUseCaseDto;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

import java.util.List;
import java.util.Map;
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

    /**
     * Converts a use case with no condition listed as shared. The controller uses
     * {@link #convert(UseCase, Map)} instead, so the editor can show which conditions other use cases also reference.
     */
    @Override
    public UseCaseDto convert(UseCase source) {
        return convert(source, Map.of());
    }

    /**
     * @param otherUses for each condition id, the other use cases that reference that condition
     *                  (from {@code UseCaseService.otherUseCasesReferencing})
     */
    public UseCaseDto convert(UseCase source, Map<Long, List<ConditionUse>> otherUses) {
        return new UseCaseDto(
                source.getId(),
                source.getArtifact().getArtifactKey(),
                source.getArtifact().getTitle(),
                source.getArtifact().getContent(),
                source.getArtifact().getTeam().getTeamId(),
                primaryActorId(source),
                source.secondaryActors().stream().map(RequirementArtifact::getId).collect(Collectors.toSet()),
                source.getUseCaseTrigger(),
                source.getPreconditions().stream().map(condition -> conditionDto(condition, otherUses)).toList(),
                source.getPostconditions().stream().map(condition -> conditionDto(condition, otherUses)).toList(),
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

    private ConditionDto conditionDto(RequirementArtifact condition, Map<Long, List<ConditionUse>> otherUses) {
        List<OtherUseCaseDto> others = otherUses.getOrDefault(condition.getId(), List.of()).stream()
                .map(use -> new OtherUseCaseDto(use.useCaseId(), use.artifactKey(), use.title()))
                .toList();
        return this.requirementArtifactToConditionDtoConverter.convert(condition, others);
    }
    // Null when the use case has no PRIMARY_ACTOR link, which only the link routes can cause (OI-77); the editor's next
    // save requires one again.
    private static Long primaryActorId(UseCase source) {
        RequirementArtifact primaryActor = source.primaryActor();
        return primaryActor == null ? null : primaryActor.getId();
    }

}
