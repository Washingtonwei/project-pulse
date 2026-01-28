package team.projectpulse.ram.collaboration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentThreadRepository extends JpaRepository<CommentThread, Long> {

    @Query("""
            select distinct t
            from CommentThread t
            left join fetch t.createdBy
            left join fetch t.comments c
            left join fetch c.author
            where t.team.teamId = :teamId
              and t.document.id = :documentId
            order by t.createdAt desc
            """)
    List<CommentThread> findCommentThreadsForDocumentWithComments(Integer teamId, Long documentId);

    @Query("""
            select distinct t
            from CommentThread t
            left join fetch t.createdBy
            left join fetch t.comments c
            left join fetch c.author
            where t.team.teamId = :teamId
              and t.documentSection.id = :documentSectionId
            order by t.createdAt desc
            """)
    List<CommentThread> findCommentThreadsForDocumentSectionWithComments(Integer teamId, Long documentSectionId);

    @Query("""
            select distinct t
            from CommentThread t
            left join fetch t.createdBy
            left join fetch t.comments c
            left join fetch c.author
            where t.team.teamId = :teamId
              and t.artifact.id = :artifactId
            order by t.createdAt desc
            """)
    List<CommentThread> findCommentThreadsForRequirementArtifactWithComments(Integer teamId, Long artifactId);

    @Query("""
            select t
            from CommentThread t
            left join fetch t.createdBy
            left join fetch t.comments c
            left join fetch c.author
            where t.team.teamId = :teamId
              and t.id = :threadId
            """)
    Optional<CommentThread> findCommentThreadByIdAndTeamIdWithComments(Long threadId, Integer teamId);
}
