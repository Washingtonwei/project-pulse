package team.projectpulse.user.userinvitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, String> {

    List<UserInvitation> findBySectionIdAndRole(Integer sectionId, String role);

}
