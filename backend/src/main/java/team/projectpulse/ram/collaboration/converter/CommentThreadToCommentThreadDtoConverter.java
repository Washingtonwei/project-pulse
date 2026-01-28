package team.projectpulse.ram.collaboration.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.collaboration.dto.CommentThreadDto;
import team.projectpulse.user.converter.PeerEvaluationUserToPeerEvaluationUserDtoConverter;

@Component
public class CommentThreadToCommentThreadDtoConverter implements Converter<CommentThread, CommentThreadDto> {

    private final PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter;
    private final CommentToCommentDtoConverter commentToCommentDtoConverter;

    public CommentThreadToCommentThreadDtoConverter(PeerEvaluationUserToPeerEvaluationUserDtoConverter peerEvaluationUserToPeerEvaluationUserDtoConverter, CommentToCommentDtoConverter commentToCommentDtoConverter) {
        this.peerEvaluationUserToPeerEvaluationUserDtoConverter = peerEvaluationUserToPeerEvaluationUserDtoConverter;
        this.commentToCommentDtoConverter = commentToCommentDtoConverter;
    }

    @Override
    public CommentThreadDto convert(CommentThread source) {
        if (source == null) return null;
        return new CommentThreadDto(
                source.getId(),
                source.getStatus(),
                source.getCreatedAt(),
                source.getCreatedBy() != null ? peerEvaluationUserToPeerEvaluationUserDtoConverter.convert(source.getCreatedBy()) : null,
                source.getComments().stream().map(this.commentToCommentDtoConverter::convert).toList()
        );
    }
}
