package team.projectpulse.ram.glossary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.TeamRepository;

@Service
@Transactional
public class GlossaryService {

    private final TeamRepository teamRepository;
    private final RequirementArtifactRepository requirementArtifactRepository;

    public GlossaryService(TeamRepository teamRepository, RequirementArtifactRepository requirementArtifactRepository) {
        this.teamRepository = teamRepository;
        this.requirementArtifactRepository = requirementArtifactRepository;
    }

    public RequirementArtifact findGlossaryTermById(Integer teamId, Long glossaryTermId) {
        return this.requirementArtifactRepository.findById(glossaryTermId).orElseThrow(() ->
                new ObjectNotFoundException("glossary term", glossaryTermId));
    }

    public RequirementArtifact saveGlossaryTerm(Integer teamId, RequirementArtifact glossaryTerm) {
        glossaryTerm.setTeam(this.teamRepository.findById(teamId).orElseThrow(() ->
                new ObjectNotFoundException("team", teamId)));
        return this.requirementArtifactRepository.save(glossaryTerm);
    }


    public RequirementArtifact updateGlossaryTermDefinition(Integer teamId, Long glossaryTermId, RequirementArtifact update) {
        return this.requirementArtifactRepository.findById(glossaryTermId).map(oldGlossaryTerm -> {
            oldGlossaryTerm.setContent(update.getContent());
            return this.requirementArtifactRepository.save(oldGlossaryTerm);
        }).orElseThrow(() -> new ObjectNotFoundException("glossary term", glossaryTermId));
    }

    public RequirementArtifact renameGlossaryTerm(Long glossaryTermId, RequirementArtifact update) {
        return this.requirementArtifactRepository.findById(glossaryTermId).map(oldGlossaryTerm -> {
            oldGlossaryTerm.setTitle(update.getTitle());
            return this.requirementArtifactRepository.save(oldGlossaryTerm);
        }).orElseThrow(() -> new ObjectNotFoundException("glossary term", glossaryTermId));
    }

}
