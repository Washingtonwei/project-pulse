package team.projectpulse.ram.glossary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactRepository;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.system.exception.ObjectNotFoundException;

@Service
@Transactional
public class GlossaryService {

    private final RequirementArtifactRepository requirementArtifactRepository;
    private final RequirementArtifactService requirementArtifactService;

    public GlossaryService(RequirementArtifactRepository requirementArtifactRepository, RequirementArtifactService requirementArtifactService) {
        this.requirementArtifactRepository = requirementArtifactRepository;
        this.requirementArtifactService = requirementArtifactService;
    }

    public RequirementArtifact findGlossaryTermById(Integer teamId, Long glossaryTermId) {
        return this.requirementArtifactRepository.findByIdAndTeamTeamIdAndType(glossaryTermId, teamId, RequirementArtifactType.GLOSSARY_TERM).orElseThrow(() ->
                new ObjectNotFoundException("glossary term", glossaryTermId));
    }

    /**
     * Creates a glossary term for the team the URL names.
     *
     * <p><strong>The type</strong> is not the caller's to set: it is fixed by the route, since this endpoint creates
     * glossary terms, so a body naming any other type is submitting a value the server owns. Left to the body it would
     * create, say, a {@code USE_CASE} row that {@link #findGlossaryTermById} cannot see (it is type-scoped) but the
     * use-case listings can. <strong>The artifact key</strong> is the student's choice (UC-GLO-create-term), checked by
     * {@link RequirementArtifactService#createRequirementArtifact}, which this method delegates to so that one place
     * checks keys for every artifact: a term sent with {@code GLO-peer-evaluation} is stored with it, while
     * {@code GLO-Peer}, {@code FR-peer} or a key another artifact of the team holds is refused (4a, 4b, 8a). A
     * glossary term has no document section home (BR-artifact-home), and the converter maps none from the body.
     */
    public RequirementArtifact saveGlossaryTerm(Integer teamId, RequirementArtifact glossaryTerm) {
        glossaryTerm.setType(RequirementArtifactType.GLOSSARY_TERM);
        return this.requirementArtifactService.createRequirementArtifact(teamId, glossaryTerm);
    }


    public RequirementArtifact updateGlossaryTermDefinition(Integer teamId, Long glossaryTermId, RequirementArtifact update) {
        return this.requirementArtifactRepository.findByIdAndTeamTeamIdAndType(glossaryTermId, teamId, RequirementArtifactType.GLOSSARY_TERM).map(oldGlossaryTerm -> {
            oldGlossaryTerm.setContent(update.getContent());
            return this.requirementArtifactRepository.save(oldGlossaryTerm);
        }).orElseThrow(() -> new ObjectNotFoundException("glossary term", glossaryTermId));
    }

    /**
     * Renames glossary term {@code glossaryTermId} of team {@code teamId} (UC-GLO-rename-term). Renaming the term never
     * renames its artifact key on its own: the request always carries the key, and only a different one is a deliberate
     * key rename, checked as a create checks a key ({@link RequirementArtifactService#renameKey}; extensions 4b, 4c,
     * 8a). For example, renaming "Artifact" to "Requirement artifact" with {@code GLO-artifact} keeps the key; sending
     * {@code GLO-requirement-artifact} as well renames it; sending a key another artifact holds refuses the whole
     * rename, so the term's name stays as it was too.
     */
    public RequirementArtifact renameGlossaryTerm(Integer teamId, Long glossaryTermId, RequirementArtifact update) {
        RequirementArtifact oldGlossaryTerm = this.requirementArtifactRepository.findByIdAndTeamTeamIdAndType(glossaryTermId, teamId, RequirementArtifactType.GLOSSARY_TERM)
                .orElseThrow(() -> new ObjectNotFoundException("glossary term", glossaryTermId));
        oldGlossaryTerm.setTitle(update.getTitle());
        // A refused key throws, and the transaction rolls back the new title with it.
        return this.requirementArtifactService.renameKey(teamId, oldGlossaryTerm, update.getArtifactKey());
    }

}
