package team.projectpulse.ram.requirement;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.Map;

@Service
@Transactional
public class RequirementArtifactService {

    private final RequirementArtifactRepository requirementArtifactRepository;
    private final TeamRepository teamRepository;
    private final ArtifactKeySequenceRepository artifactKeySequenceRepository;

    public RequirementArtifactService(RequirementArtifactRepository requirementArtifactRepository, TeamRepository teamRepository, ArtifactKeySequenceRepository artifactKeySequenceRepository) {
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.teamRepository = teamRepository;
        this.artifactKeySequenceRepository = artifactKeySequenceRepository;
    }

    public Page<RequirementArtifact> findByCriteria(Integer teamId, Map<String, String> searchCriteria, Pageable pageable) {
        Specification<RequirementArtifact> spec = Specification.unrestricted(); // Start with an unrestricted specification

        if (StringUtils.hasLength(searchCriteria.get("type"))) {
            spec = spec.and(RequirementArtifactSpecs.hasType(RequirementArtifactType.valueOf(searchCriteria.get("type"))));
        }

        if (StringUtils.hasLength(searchCriteria.get("artifactKey"))) {
            spec = spec.and(RequirementArtifactSpecs.hasArtifactKeyLike(searchCriteria.get("artifactKey")));
        }

        if (StringUtils.hasLength(searchCriteria.get("title"))) {
            spec = spec.and(RequirementArtifactSpecs.hasTitleLike(searchCriteria.get("title")));
        }

        if (StringUtils.hasLength(searchCriteria.get("content"))) {
            spec = spec.and(RequirementArtifactSpecs.hasContentLike(searchCriteria.get("content")));
        }

        if (StringUtils.hasLength(searchCriteria.get("priority"))) {
            spec = spec.and(RequirementArtifactSpecs.hasPriority(Priority.valueOf(searchCriteria.get("priority"))));
        }

        spec = spec.and(RequirementArtifactSpecs.hasTeamId(teamId));

        return this.requirementArtifactRepository.findAll(spec, pageable);
    }

    public RequirementArtifact findRequirementArtifactById(Integer teamId, Long requirementArtifactId) {
        return this.requirementArtifactRepository.findById(requirementArtifactId).orElseThrow(() ->
                new ObjectNotFoundException("requirement artifact", requirementArtifactId));
    }

    public RequirementArtifact saveRequirementArtifact(Integer teamId, RequirementArtifact newRequirementArtifact) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        newRequirementArtifact.setTeam(team);

        if (newRequirementArtifact.getType() == null) {
            throw new IllegalArgumentException("RequirementArtifact.type is required to generate artifactKey.");
        }

        // Generate and set the artifact key
        newRequirementArtifact.setArtifactKey(generateNextArtifactKey(teamId, newRequirementArtifact.getType()));

        return this.requirementArtifactRepository.save(newRequirementArtifact);
    }

    public RequirementArtifact updateRequirementArtifact(Integer teamId, Long requirementArtifactId, RequirementArtifact update) {
        return this.requirementArtifactRepository.findById(requirementArtifactId).map(oldRequirementArtifact -> {
            oldRequirementArtifact.setTitle(update.getTitle());
            oldRequirementArtifact.setContent(update.getContent());
            oldRequirementArtifact.setPriority(update.getPriority());
            oldRequirementArtifact.setNotes(update.getNotes());
            return this.requirementArtifactRepository.save(oldRequirementArtifact);
        }).orElseThrow(() -> new ObjectNotFoundException("requirement artifact", requirementArtifactId));
    }

    public void deleteRequirementArtifact(Integer teamId, Long requirementArtifactId) {
        RequirementArtifact existing = this.findRequirementArtifactById(teamId, requirementArtifactId);
        this.requirementArtifactRepository.delete(existing);
    }

    private String generateNextArtifactKey(Integer teamId, RequirementArtifactType type) {
        String prefix = ArtifactKeyPrefix.of(type);

        // Lock the counter row for this (team, type)
        ArtifactKeySequence seq = artifactKeySequenceRepository.findForUpdate(teamId, type)
                .orElseGet(() -> artifactKeySequenceRepository.save(new ArtifactKeySequence(
                        teamRepository.getReferenceById(teamId), type
                )));

        long number = seq.getNextNumber();
        seq.setNextNumber(number + 1);
        artifactKeySequenceRepository.save(seq);

        return prefix + "-" + number;
    }

}
