package team.projectpulse.ram.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.ram.requirement.RequirementArtifactService;
import team.projectpulse.system.exception.ObjectNotFoundException;

import java.util.Set;

@Service
@Transactional
public class UseCaseService {

    private final UseCaseRepository useCaseRepository;
    private final RequirementArtifactService requirementArtifactService;


    public UseCaseService(UseCaseRepository useCaseRepository, RequirementArtifactService requirementArtifactService) {
        this.useCaseRepository = useCaseRepository;
        this.requirementArtifactService = requirementArtifactService;
    }

    /**
     * Finds a use case by ID with all related entities fully loaded.
     * Uses the 3-query approach to avoid big Cartesian products:
     * 1. Load use case with scalar relationships (artifact, primaryActor)
     * 2. Load ManyToMany collections (secondaryActors, preconditions, postconditions)
     * 3. Load the main steps graph (mainSteps -> extensions -> extension steps)
     * <p>
     * All queries execute within the same transaction/persistence context,
     * so Hibernate automatically merges them into a single use case entity.
     *
     * @param id the use case ID
     * @return the fully loaded use case
     * @throws ObjectNotFoundException if the use case is not found
     */
    public UseCase findUseCaseByIdWithFullGraph(Long id) {
        // Query 1: Load UseCase with scalar relationships
        UseCase useCase = this.useCaseRepository.findByIdWithScalars(id)
                .orElseThrow(() -> new ObjectNotFoundException("use case", id));

        // Query 2: Load ManyToMany collections
        // This populates secondaryActors, preconditions, postconditions
        this.useCaseRepository.findByIdWithManyToMany(id);

        // Query 3: Load the step hierarchy
        // This populates mainSteps -> extensions -> steps
        this.useCaseRepository.findByIdWithStepGraph(id);

        // Return the fully loaded entity
        // All collections are now populated in the same instance
        return useCase;
    }

    /**
     * Finds a UseCase by ID with only basic information (no collections loaded).
     * Useful for listing or when you don't need the full graph.
     *
     * @param id the UseCase ID
     * @return the UseCase with only artifact loaded
     * @throws ObjectNotFoundException if the UseCase is not found
     */
    public UseCase findUseCaseByIdBasic(Long id) {
        return this.useCaseRepository.findByIdWithScalars(id)
                .orElseThrow(() -> new ObjectNotFoundException("use case", id));
    }

    public UseCase saveUseCase(Integer teamId, UseCase useCase) {
        this.requirementArtifactService.saveRequirementArtifact(teamId, useCase.getArtifact());
        return this.useCaseRepository.save(useCase);
    }

    public UseCase updateUseCase(Long useCaseId, UseCase update) {
        UseCase oldUseCase = this.useCaseRepository.findById(useCaseId)
                .orElseThrow(() -> new ObjectNotFoundException("use case", useCaseId));

        // Update artifact fields
        oldUseCase.getArtifact().setTitle(update.getArtifact().getTitle());
        oldUseCase.getArtifact().setContent(update.getArtifact().getContent());
        oldUseCase.getArtifact().setPriority(update.getArtifact().getPriority());
        oldUseCase.getArtifact().setNotes(update.getArtifact().getNotes());

        // Update scalar relationships
        oldUseCase.setPrimaryActor(update.getPrimaryActor());
        oldUseCase.setUseCaseTrigger(update.getUseCaseTrigger());

        // Update ManyToMany collections
        oldUseCase.getSecondaryActors().clear();
        oldUseCase.getSecondaryActors().addAll(update.getSecondaryActors());

        oldUseCase.getPreconditions().clear();
        oldUseCase.getPreconditions().addAll(update.getPreconditions());

        oldUseCase.getPostconditions().clear();
        oldUseCase.getPostconditions().addAll(update.getPostconditions());

        // Update main steps
        oldUseCase.replaceMainSteps(update.getMainSteps());

        return this.useCaseRepository.save(oldUseCase);
    }

    private <T> void updateCollection(Set<T> oldCollection, Set<T> newCollection) {
        oldCollection.clear();
        if (newCollection != null) {
            oldCollection.addAll(newCollection);
        }
    }

}

