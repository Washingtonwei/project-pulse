package team.projectpulse.ram.collaboration.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.collaboration.Comment;
import team.projectpulse.ram.collaboration.dto.CommentDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

@Component
public class CommentToCommentDtoConverter implements Converter<Comment, CommentDto> {

    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;

    public CommentToCommentDtoConverter(PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter) {
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
    }

    @Override
    public CommentDto convert(Comment source) {
        if (source == null) return null;
        return new CommentDto(
                source.getId(),
                source.getAuthor() != null ? peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getAuthor()) : null,
                source.getContent(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }
}
