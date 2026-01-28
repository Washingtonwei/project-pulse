package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.dto.DocumentSectionDto;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.ram.requirement.converter.RequirementArtifactToRequirementArtifactDtoConverter;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

@Component
public class DocumentSectionToDocumentSectionDtoConverter implements Converter<DocumentSection, DocumentSectionDto> {

    private final RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactConverter;
    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserConverter;
    private final DocumentSectionLockToDocumentSectionLockDtoConverter documentSectionLockToDocumentSectionLockDtoConverter;

    public DocumentSectionToDocumentSectionDtoConverter(RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactConverter, PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserConverter, DocumentSectionLockToDocumentSectionLockDtoConverter documentSectionLockToDocumentSectionLockDtoConverter) {
        this.requirementArtifactConverter = requirementArtifactConverter;
        this.peerEvaluationUserConverter = peerEvaluationUserConverter;
        this.documentSectionLockToDocumentSectionLockDtoConverter = documentSectionLockToDocumentSectionLockDtoConverter;
    }

    @Override
    public DocumentSectionDto convert(DocumentSection source) {
        return new DocumentSectionDto(
                source.getId(),
                source.getSectionKey(),
                source.getType(),
                source.getTitle(),
                source.getContent(),
                source.getType().equals(SectionType.LIST) ?
                        source.getRequirementArtifacts() != null ?
                                source.getRequirementArtifacts().stream()
                                        .map(this.requirementArtifactConverter::convert)
                                        .toList()
                                : null
                        : null,
                source.getGuidance(),
                source.getCreatedAt(),
                source.getUpdatedAt(),
                source.getCreatedBy() != null ? this.peerEvaluationUserConverter.convert(source.getCreatedBy()) : null,
                source.getUpdatedBy() != null ? this.peerEvaluationUserConverter.convert(source.getUpdatedBy()) : null,
                this.documentSectionLockToDocumentSectionLockDtoConverter.convert(source.getLock())
        );
    }
}
