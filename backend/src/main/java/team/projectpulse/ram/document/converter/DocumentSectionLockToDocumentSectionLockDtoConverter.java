package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentSectionLock;
import team.projectpulse.ram.document.dto.DocumentSectionLockDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

import java.time.Instant;

@Component
public class DocumentSectionLockToDocumentSectionLockDtoConverter implements Converter<DocumentSectionLock, DocumentSectionLockDto> {

    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;


    public DocumentSectionLockToDocumentSectionLockDtoConverter(
            PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter) {
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
    }

    @Override
    public DocumentSectionLockDto convert(DocumentSectionLock source) {
        Instant now = Instant.now();
        boolean locked = source.isLocked(now);

        if (!locked) {
            // Section is effectively unlocked
            return new DocumentSectionLockDto(
                    false,
                    source.getDocumentSection().getId(),
                    null,
                    null,
                    null,
                    null
            );
        }

        return new DocumentSectionLockDto(
                true,
                source.getDocumentSection().getId(),
                source.getLockedBy() != null ?
                        peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getLockedBy()) : null,
                source.getLockedAt(),
                source.getExpiresAt(),
                source.getReason()
        );
    }
}

