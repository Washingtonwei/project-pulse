package team.projectpulse.ram.usecase;

import java.util.Set;

/**
 * The references a use case save names (its actors, by id), carried from the request to {@link UseCaseService} beside
 * the converted use case. The DTO converter maps fields only and never turns an id into an artifact, so a reference
 * can only be made by the service, through a lookup scoped to the team in the route (OI-46, BR-team-scoped-access).
 *
 * @param primaryActorId    the primary actor's artifact id; required, and refused by the service when null
 * @param secondaryActorIds the secondary actors' artifact ids; a null set is read as empty
 */
public record UseCaseReferences(Long primaryActorId, Set<Long> secondaryActorIds) {

    public UseCaseReferences {
        secondaryActorIds = secondaryActorIds == null ? Set.of() : Set.copyOf(secondaryActorIds);
    }
}
