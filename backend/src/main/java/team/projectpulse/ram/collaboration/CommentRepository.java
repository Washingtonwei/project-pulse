package team.projectpulse.ram.collaboration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndCommentThreadIdAndCommentThreadTeamTeamId(Long commentId, Long commentThreadId, Integer teamId);
}