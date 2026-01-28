package team.projectpulse.ram.requirement.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

@Component
public class RequirementArtifactToRequirementArtifactDtoConverter implements Converter<RequirementArtifact, RequirementArtifactDto> {

    private final ArtifactLinkToUpstreamLinkViewDtoConverter artifactLinkToUpstreamLinkViewDtoConverter;
    private final ArtifactLinkToDownstreamLinkViewDtoConverter artifactLinkToDownstreamLinkViewDtoConverter;
    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;

    public RequirementArtifactToRequirementArtifactDtoConverter(ArtifactLinkToUpstreamLinkViewDtoConverter artifactLinkToUpstreamLinkViewDtoConverter, ArtifactLinkToDownstreamLinkViewDtoConverter artifactLinkToDownstreamLinkViewDtoConverter, PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter) {
        this.artifactLinkToUpstreamLinkViewDtoConverter = artifactLinkToUpstreamLinkViewDtoConverter;
        this.artifactLinkToDownstreamLinkViewDtoConverter = artifactLinkToDownstreamLinkViewDtoConverter;
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
    }

    @Override
    public RequirementArtifactDto convert(RequirementArtifact source) {
        return new RequirementArtifactDto(
                source.getId(),
                source.getType(),
                source.getArtifactKey(),
                source.getTitle(),
                source.getContent(),
                source.getPriority(),
                source.getSourceDocumentSection() != null ? source.getSourceDocumentSection().getId() : null,
                source.getNotes(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getCreatedBy() != null ? peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getCreatedBy()) : null,
                source.getUpdatedBy() != null ? peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getUpdatedBy()) : null
        );
    }

}
