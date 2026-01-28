package team.projectpulse.system;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import team.projectpulse.user.PeerEvaluationUser;
import team.projectpulse.user.UserRepository;

import java.util.Optional;

@Component
public class PeerEvaluationUserAuditorAware implements AuditorAware<PeerEvaluationUser> {

    private final UserUtils userUtils;
    private final UserRepository userRepository;


    public PeerEvaluationUserAuditorAware(UserUtils userUtils, UserRepository userRepository) {
        this.userUtils = userUtils;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<PeerEvaluationUser> getCurrentAuditor() {
        Integer userId = this.userUtils.getUserId();
        if (userId == null) return Optional.empty();
        return Optional.of(this.userRepository.getReferenceById(userId));
    }

}
