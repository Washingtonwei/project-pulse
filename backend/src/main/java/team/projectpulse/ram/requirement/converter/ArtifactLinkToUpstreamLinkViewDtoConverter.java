package team.projectpulse.ram.requirement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.ArtifactLink;
import team.projectpulse.ram.requirement.dto.ArtifactLinkViewDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

@Component
public class ArtifactLinkToUpstreamLinkViewDtoConverter implements Converter<ArtifactLink, ArtifactLinkViewDto> {

    private final RequirementArtifactToRequirementArtifactSummaryDtoConverter requirementArtifactToRequirementArtifactSummaryDtoConverter;
    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;

    public ArtifactLinkToUpstreamLinkViewDtoConverter(RequirementArtifactToRequirementArtifactSummaryDtoConverter requirementArtifactToRequirementArtifactSummaryDtoConverter, PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter) {
        this.requirementArtifactToRequirementArtifactSummaryDtoConverter = requirementArtifactToRequirementArtifactSummaryDtoConverter;
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
    }

    @Override
    public ArtifactLinkViewDto convert(ArtifactLink source) {
        return new ArtifactLinkViewDto(
                source.getId(),
                source.getType(),
                source.getNotes(),
                this.requirementArtifactToRequirementArtifactSummaryDtoConverter.convert(source.getSourceArtifact()),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getCreatedBy() != null ? peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getCreatedBy()) : null,
                source.getUpdatedBy() != null ? peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getUpdatedBy()) : null
        );
    }
}
