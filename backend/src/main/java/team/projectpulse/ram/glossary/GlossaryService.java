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
     * <p>Three values the request body carries are not the caller's to set, and each is taken back here rather
     * than trusted. <strong>The type</strong> is fixed by the route: this endpoint creates glossary terms, so a
     * body naming any other type is submitting a value the server owns. Left to the body it would create, say, a
     * {@code USE_CASE} row that {@link #findGlossaryTermById} cannot see (it is type-scoped) but the use-case
     * listings can. <strong>The artifact key</strong> is minted from the team's {@code GLO} sequence by
     * {@link RequirementArtifactService#saveRequirementArtifact}, which this method delegates to so that one place
     * mints keys for every artifact; a body-chosen key such as {@code UC-1} would otherwise collide with a real
     * key that sequence has already handed out or will hand out later, and an omitted one left the term keyless.
     * <strong>The source document section</strong> is cleared: a term created here belongs to no section, and the
     * association is owned by {@code RequirementArtifact}, so a {@code sourceSectionId} naming another team's
     * section would insert this row into that team's ordered section list.
     *
     * <p>That last line is a stopgap at the wrong layer. The section is resolved by
     * {@code RequirementArtifactDtoToRequirementArtifactConverter} through an unscoped {@code findById}, which is
     * the converter-resolution half of OI-46 and is fixed by moving the resolution into the services behind a
     * team-scoped finder. Delete this line when that lands.
     */
    public RequirementArtifact saveGlossaryTerm(Integer teamId, RequirementArtifact glossaryTerm) {
        glossaryTerm.setType(RequirementArtifactType.GLOSSARY_TERM);
        glossaryTerm.setSourceDocumentSection(null);
        return this.requirementArtifactService.saveRequirementArtifact(teamId, glossaryTerm);
    }


    public RequirementArtifact updateGlossaryTermDefinition(Integer teamId, Long glossaryTermId, RequirementArtifact update) {
        return this.requirementArtifactRepository.findByIdAndTeamTeamIdAndType(glossaryTermId, teamId, RequirementArtifactType.GLOSSARY_TERM).map(oldGlossaryTerm -> {
            oldGlossaryTerm.setContent(update.getContent());
            return this.requirementArtifactRepository.save(oldGlossaryTerm);
        }).orElseThrow(() -> new ObjectNotFoundException("glossary term", glossaryTermId));
    }

    public RequirementArtifact renameGlossaryTerm(Integer teamId, Long glossaryTermId, RequirementArtifact update) {
        return this.requirementArtifactRepository.findByIdAndTeamTeamIdAndType(glossaryTermId, teamId, RequirementArtifactType.GLOSSARY_TERM).map(oldGlossaryTerm -> {
            oldGlossaryTerm.setTitle(update.getTitle());
            return this.requirementArtifactRepository.save(oldGlossaryTerm);
        }).orElseThrow(() -> new ObjectNotFoundException("glossary term", glossaryTermId));
    }

}
