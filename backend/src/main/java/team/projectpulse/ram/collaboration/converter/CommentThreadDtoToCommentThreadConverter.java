package team.projectpulse.ram.collaboration.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.collaboration.dto.CommentThreadDto;

@Component
public class CommentThreadDtoToCommentThreadConverter implements Converter<CommentThreadDto, CommentThread> {
    @Override
    public CommentThread convert(CommentThreadDto source) {
        CommentThread commentThread = new CommentThread();
        commentThread.setId(source.id());
        commentThread.setStatus(source.status());
        return commentThread;
    }
}
