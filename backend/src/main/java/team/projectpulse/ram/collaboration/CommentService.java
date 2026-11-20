package team.projectpulse.ram.collaboration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.collaboration.converter.CommentDtoToCommentConverter;
import team.projectpulse.ram.collaboration.dto.CommentDto;
import team.projectpulse.ram.document.DocumentRepository;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.DocumentSectionRepository;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.system.exception.ObjectNotFoundException;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentThreadRepository commentThreadRepository;
    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final DocumentSectionRepository documentSectionRepository;
    private final RequirementArtifactRepository requirementArtifactRepository;
    private final CommentDtoToCommentConverter commentDtoToCommentConverter;

    public CommentService(CommentThreadRepository commentThreadRepository, CommentRepository commentRepository, DocumentRepository documentRepository, DocumentSectionRepository documentSectionRepository, RequirementArtifactRepository requirementArtifactRepository, CommentDtoToCommentConverter commentDtoToCommentConverter) {
        this.commentThreadRepository = commentThreadRepository;
        this.commentRepository = commentRepository;
        this.documentRepository = documentRepository;
        this.documentSectionRepository = documentSectionRepository;
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.commentDtoToCommentConverter = commentDtoToCommentConverter;
    }

    // -------- list threads --------
    public List<CommentThread> listThreadsForDocument(Integer teamId, Long documentId) {
//        authz.assertCanViewThreads(teamId);

        // The document must belong to this team; an out-of-scope id is simply not found.
        documentRepository.findByIdAndTeamTeamId(documentId, teamId).orElseThrow(() -> new ObjectNotFoundException("document", documentId));
        return commentThreadRepository.findCommentThreadsForDocumentWithComments(teamId, documentId);
    }

    public List<CommentThread> listThreadsForDocumentSection(Integer teamId, Long documentId, Long documentSectionId) {
//        authz.assertCanViewThreads(teamId);

        // The section must belong to this document and this team.
        documentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId(documentSectionId, documentId, teamId).orElseThrow(() -> new ObjectNotFoundException("document section", documentSectionId));

        return commentThreadRepository.findCommentThreadsForDocumentSectionWithComments(teamId, documentSectionId);
    }

    public List<CommentThread> listThreadsForArtifact(Integer teamId, Long artifactId) {
//        authz.assertCanViewThreads(teamId);

        requirementArtifactRepository.findByIdAndTeamTeamId(artifactId, teamId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
        return commentThreadRepository.findCommentThreadsForRequirementArtifactWithComments(teamId, artifactId);
    }

    // -------- create thread (with first comment) --------
    public CommentThread createThreadForDocument(Integer teamId, Long documentId, CommentDto firstCommentDto) {
//        authz.assertCanComment(teamId);

        RequirementDocument doc = documentRepository.findByIdAndTeamTeamId(documentId, teamId).orElseThrow(() -> new ObjectNotFoundException("document", documentId));

//        var user = currentUserService.getCurrentUserOrThrow();

        CommentThread thread = new CommentThread();
        thread.setTeam(doc.getTeam()); // or load Team by teamId
        // Both sides of the association: the thread's foreign key and the document's in-memory set, which its cascade walks.
        doc.addCommentThread(thread);
//        thread.setCreatedBy(user);

        Comment comment = commentDtoToCommentConverter.convert(firstCommentDto);
//        comment.setAuthor(user);
        thread.addComment(comment);

        return commentThreadRepository.save(thread);
    }

    public CommentThread createThreadForSection(Integer teamId, Long documentId, Long sectionId, CommentDto firstCommentDto) {
//        authz.assertCanComment(teamId);

        DocumentSection section = documentSectionRepository.findByIdAndDocumentIdAndDocumentTeamTeamId(sectionId, documentId, teamId).orElseThrow(() -> new ObjectNotFoundException("section", sectionId));

//        var user = currentUserService.getCurrentUserOrThrow();

        CommentThread thread = new CommentThread();
        thread.setTeam(section.getDocument().getTeam());
        // Both sides, for the document and for the section: each sets the thread's foreign key and adds the thread to
        // its own in-memory set, which its cascade walks.
        section.getDocument().addCommentThread(thread);
        section.addCommentThread(thread);
//        thread.setCreatedBy(user);

        Comment comment = commentDtoToCommentConverter.convert(firstCommentDto);
//        comment.setAuthor(user);
        thread.addComment(comment);

        return commentThreadRepository.save(thread);
    }

    public CommentThread createThreadForArtifact(Integer teamId, Long artifactId, CommentDto firstCommentDto) {
//        authz.assertCanComment(teamId);

        RequirementArtifact artifact = requirementArtifactRepository.findByIdAndTeamTeamId(artifactId, teamId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

//        var user = currentUserService.getCurrentUserOrThrow();

        CommentThread thread = new CommentThread();
        thread.setTeam(artifact.getTeam());
        // Both sides: the thread's foreign key and the artifact's in-memory set. The set is what the artifact's cascade
        // walks, so a thread missing from it would not be deleted with the artifact in the same transaction.
        artifact.addCommentThread(thread);
//        thread.setCreatedBy(user);

        Comment comment = commentDtoToCommentConverter.convert(firstCommentDto);
//        comment.setAuthor(user);
        thread.addComment(comment);

        return commentThreadRepository.save(thread);
    }

    // -------- thread CRUD --------
    public CommentThread getThread(Integer teamId, Long threadId) {
//        authz.assertCanViewThreads(teamId);
        return commentThreadRepository.findCommentThreadByIdAndTeamIdWithComments(threadId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("comment thread", threadId));
    }

    public CommentThread updateThreadStatus(Integer teamId, Long threadId, CommentThread update) {
//        authz.assertCanModerateThread(teamId);
        CommentThread thread = getThread(teamId, threadId);
        thread.setStatus(update.getStatus());
        return commentThreadRepository.save(thread);
    }

    public void deleteThread(Integer teamId, Long threadId) {
//        authz.assertCanModerateThread(teamId);
        CommentThread thread = getThread(teamId, threadId);
        commentThreadRepository.delete(thread);
    }

    // -------- comments --------
    public CommentThread addCommentToThread(Integer teamId, Long threadId, CommentDto commentDto) {
//        authz.assertCanComment(teamId);
        CommentThread thread = getThread(teamId, threadId);

//        var user = currentUserService.getCurrentUserOrThrow();

        Comment comment = commentDtoToCommentConverter.convert(commentDto);
//        comment.setAuthor(user);
        thread.addComment(comment);

        return commentThreadRepository.save(thread);
    }

    public Comment getComment(Integer teamId, Long commentThreadId, Long commentId) {
//        authz.assertCanViewThreads(teamId);
        return commentRepository.findByIdAndCommentThreadIdAndCommentThreadTeamTeamId(commentId, commentThreadId, teamId)
                .orElseThrow(() -> new ObjectNotFoundException("comment", commentId));
    }

    public Comment updateComment(Integer teamId, Long threadId, Long commentId, String newContent) {
        Comment comment = getComment(teamId, threadId, commentId);
//        var user = currentUserService.getCurrentUserOrThrow();
//        authz.assertCanEditComment(teamId, comment.getAuthor().getId(), user.getId());

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    public void deleteComment(Integer teamId, Long threadId, Long commentId) {
        Comment comment = getComment(teamId, threadId, commentId);
//        var user = currentUserService.getCurrentUserOrThrow();
//        authz.assertCanEditComment(teamId, comment.getAuthor().getId(), user.getId());

        commentRepository.delete(comment);
    }

}
