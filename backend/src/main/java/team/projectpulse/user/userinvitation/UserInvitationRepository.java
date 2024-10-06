package team.projectpulse.user.userinvitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, String> {
}
