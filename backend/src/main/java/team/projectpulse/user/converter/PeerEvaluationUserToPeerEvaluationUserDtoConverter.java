package team.projectpulse.user.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

@Component
public class PeerEvaluationUserToPeerEvaluationUserDtoConverter implements Converter<PeerEvaluationUser, PeerEvaluationUserDto> {
    @Override
    public PeerEvaluationUserDto convert(PeerEvaluationUser source) {
        return new PeerEvaluationUserDto(source.getId(),
                source.getFirstName() + " " + source.getLastName());
    }
}
