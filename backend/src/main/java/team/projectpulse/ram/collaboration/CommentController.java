package team.projectpulse.ram.collaboration;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.collaboration.converter.CommentThreadDtoToCommentThreadConverter;
import team.projectpulse.ram.collaboration.converter.CommentThreadToCommentThreadDtoConverter;
import team.projectpulse.ram.collaboration.converter.CommentToCommentDtoConverter;
import team.projectpulse.ram.collaboration.dto.CommentDto;
import team.projectpulse.ram.collaboration.dto.CommentThreadDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class CommentController {

    private final CommentService commentService;
    private final CommentThreadToCommentThreadDtoConverter commentThreadToCommentThreadDtoConverter;
    private final CommentThreadDtoToCommentThreadConverter commentThreadDtoToCommentThreadConverter;
    private final CommentToCommentDtoConverter commentToCommentDtoConverter;


    public CommentController(CommentService commentService, CommentThreadToCommentThreadDtoConverter commentThreadToCommentThreadDtoConverter, CommentThreadDtoToCommentThreadConverter commentThreadDtoToCommentThreadConverter, CommentToCommentDtoConverter commentToCommentDtoConverter) {
        this.commentService = commentService;
        this.commentThreadToCommentThreadDtoConverter = commentThreadToCommentThreadDtoConverter;
        this.commentThreadDtoToCommentThreadConverter = commentThreadDtoToCommentThreadConverter;
        this.commentToCommentDtoConverter = commentToCommentDtoConverter;
    }

    @GetMapping("/teams/{teamId}/documents/{documentId}/comment-threads")
    public Result listCommentThreadsForDocument(@PathVariable Integer teamId, @PathVariable Long documentId) {
        List<CommentThreadDto> dtos = commentService.listThreadsForDocument(teamId, documentId)
                .stream().map(commentThreadToCommentThreadDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find comment threads successfully", dtos);
    }

    @PostMapping("/teams/{teamId}/documents/{documentId}/comment-threads")
    public Result createCommentThreadForDocument(@PathVariable Integer teamId, @PathVariable Long documentId, @Valid @RequestBody CommentDto firstComment) {
        CommentThreadDto dto = commentThreadToCommentThreadDtoConverter.convert(
                commentService.createThreadForDocument(teamId, documentId, firstComment)
        );
        return new Result(true, StatusCode.SUCCESS, "Create comment thread successfully", dto);
    }

    @GetMapping("/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/comment-threads")
    public Result listCommentThreadsForDocumentSection(@PathVariable Integer teamId, @PathVariable Long documentId, @PathVariable Long documentSectionId) {
        List<CommentThreadDto> dtos = commentService.listThreadsForDocumentSection(teamId, documentId, documentSectionId)
                .stream().map(commentThreadToCommentThreadDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find comment threads successfully", dtos);
    }

    @PostMapping("/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/comment-threads")
    public Result createCommentThreadForDocumentSection(@PathVariable Integer teamId, @PathVariable Long documentId, @PathVariable Long documentSectionId, @Valid @RequestBody CommentDto firstComment) {
        CommentThreadDto dto = commentThreadToCommentThreadDtoConverter.convert(
                commentService.createThreadForSection(teamId, documentId, documentSectionId, firstComment)
        );
        return new Result(true, StatusCode.SUCCESS, "Create comment thread successfully", dto);
    }

    @GetMapping("/teams/{teamId}/requirement-artifacts/{artifactId}/comment-threads")
    public Result listCommentThreadsForRequirementArtifact(@PathVariable Integer teamId, @PathVariable Long artifactId) {
        List<CommentThreadDto> dtos = commentService.listThreadsForArtifact(teamId, artifactId)
                .stream().map(commentThreadToCommentThreadDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find comment threads successfully", dtos);
    }

    @PostMapping("/teams/{teamId}/requirement-artifacts/{artifactId}/comment-threads")
    public Result createCommentThreadForRequirementArtifact(@PathVariable Integer teamId, @PathVariable Long artifactId, @Valid @RequestBody CommentDto firstComment) {
        CommentThreadDto dto = commentThreadToCommentThreadDtoConverter.convert(
                commentService.createThreadForArtifact(teamId, artifactId, firstComment)
        );
        return new Result(true, StatusCode.SUCCESS, "Create comment thread successfully", dto);
    }

    @GetMapping("/teams/{teamId}/comment-threads/{commentThreadId}")
    public Result getCommentThread(@PathVariable Integer teamId, @PathVariable Long commentThreadId) {
        CommentThreadDto dto = commentThreadToCommentThreadDtoConverter.convert(commentService.getThread(teamId, commentThreadId));
        return new Result(true, StatusCode.SUCCESS, "Find comment thread successfully", dto);
    }

    @PatchMapping("/teams/{teamId}/comment-threads/{commentThreadId}")
    public Result updateCommentThreadStatus(@PathVariable Integer teamId, @PathVariable Long commentThreadId, @RequestBody CommentThreadDto commentThreadDto) {
        CommentThread update = commentThreadDtoToCommentThreadConverter.convert(commentThreadDto);
        CommentThread updatedThread = commentService.updateThreadStatus(teamId, commentThreadId, update);
        CommentThreadDto dto = commentThreadToCommentThreadDtoConverter.convert(updatedThread);
        return new Result(true, StatusCode.SUCCESS, "Update comment thread successfully", dto);
    }

    @DeleteMapping("/teams/{teamId}/comment-threads/{commentThreadId}")
    public Result deleteCommentThread(@PathVariable Integer teamId, @PathVariable Long commentThreadId) {
        commentService.deleteThread(teamId, commentThreadId);
        return new Result(true, StatusCode.SUCCESS, "Delete comment thread successfully", null);
    }

    @PostMapping("/teams/{teamId}/comment-threads/{commentThreadId}/comments")
    public Result addCommentToCommentThread(@PathVariable Integer teamId, @PathVariable Long commentThreadId, @Valid @RequestBody CommentDto comment) {
        CommentThreadDto dto = commentThreadToCommentThreadDtoConverter.convert(
                commentService.addCommentToThread(teamId, commentThreadId, comment)
        );
        return new Result(true, StatusCode.SUCCESS, "Add comment successfully", dto);
    }

    @GetMapping("/teams/{teamId}/comment-threads/{commentThreadId}/comments/{commentId}")
    public Result getComment(@PathVariable Integer teamId, @PathVariable Long commentThreadId, @PathVariable Long commentId) {
        Comment comment = commentService.getComment(teamId, commentThreadId, commentId);
        CommentDto dto = commentToCommentDtoConverter.convert(comment);
        return new Result(true, StatusCode.SUCCESS, "Find comment successfully", dto);
    }

    @PatchMapping("/teams/{teamId}/comment-threads/{commentThreadId}/comments/{commentId}")
    public Result updateComment(@PathVariable Integer teamId, @PathVariable Long commentThreadId, @PathVariable Long commentId, @Valid @RequestBody CommentDto body) {
        Comment updated = commentService.updateComment(teamId, commentThreadId, commentId, body.content());
        CommentDto dto = commentToCommentDtoConverter.convert(updated);
        return new Result(true, StatusCode.SUCCESS, "Update comment successfully", dto);
    }

    @DeleteMapping("/teams/{teamId}/comment-threads/{commentThreadId}/comments/{commentId}")
    public Result deleteComment(@PathVariable Integer teamId, @PathVariable Long commentThreadId, @PathVariable Long commentId) {
        commentService.deleteComment(teamId, commentThreadId, commentId);
        return new Result(true, StatusCode.SUCCESS, "Delete comment successfully", null);
    }

}
