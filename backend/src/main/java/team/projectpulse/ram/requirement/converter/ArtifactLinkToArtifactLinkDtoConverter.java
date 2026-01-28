package team.projectpulse.ram.requirement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.ArtifactLink;
import team.projectpulse.ram.requirement.dto.ArtifactLinkDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

@Component
public class ArtifactLinkToArtifactLinkDtoConverter implements Converter<ArtifactLink, ArtifactLinkDto> {

    private final RequirementArtifactToRequirementArtifactSummaryDtoConverter requirementArtifactToRequirementArtifactSummaryDtoConverter;
    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;

    public ArtifactLinkToArtifactLinkDtoConverter(RequirementArtifactToRequirementArtifactSummaryDtoConverter requirementArtifactToRequirementArtifactSummaryDtoConverter, PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter) {
        this.requirementArtifactToRequirementArtifactSummaryDtoConverter = requirementArtifactToRequirementArtifactSummaryDtoConverter;
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
    }

    @Override
    public ArtifactLinkDto convert(ArtifactLink source) {
        return new ArtifactLinkDto(
                source.getId(),
                source.getType(),
                source.getNotes(),
                this.requirementArtifactToRequirementArtifactSummaryDtoConverter.convert(source.getSourceArtifact()),
                this.requirementArtifactToRequirementArtifactSummaryDtoConverter.convert(source.getTargetArtifact()),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getCreatedBy() != null ? this.peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getCreatedBy()) : null,
                source.getUpdatedBy() != null ? this.peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getUpdatedBy()) : null
        );
    }
}
