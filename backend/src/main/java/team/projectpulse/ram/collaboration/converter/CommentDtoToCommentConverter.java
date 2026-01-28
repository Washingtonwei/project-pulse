package team.projectpulse.ram.collaboration.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.collaboration.Comment;
import team.projectpulse.ram.collaboration.dto.CommentDto;

@Component
public class CommentDtoToCommentConverter implements Converter<CommentDto, Comment> {
    @Override
    public Comment convert(CommentDto source) {
        if (source == null) return null;
        Comment c = new Comment();
        c.setContent(source.content());
        return c;
    }
}
