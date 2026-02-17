package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseLock;
import team.projectpulse.ram.usecase.dto.UseCaseLockDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

import java.time.Instant;

@Component
public class UseCaseLockToUseCaseLockDtoConverter implements Converter<UseCaseLock, UseCaseLockDto> {

    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;


    public UseCaseLockToUseCaseLockDtoConverter(PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter) {
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
    }

    @Override
    public UseCaseLockDto convert(UseCaseLock source) {
        Instant now = Instant.now();
        boolean locked = source.isLocked(now);

        if (!locked) {
            return new UseCaseLockDto(
                    false,
                    source.getUseCase().getId(),
                    null,
                    null,
                    null,
                    null
            );
        }

        return new UseCaseLockDto(
                true,
                source.getUseCase().getId(),
                source.getLockedBy() != null ?
                        peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getLockedBy()) : null,
                source.getLockedAt(),
                source.getExpiresAt(),
                source.getReason()
        );
    }
    
}
