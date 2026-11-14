package team.projectpulse.ram.usecase;

import java.util.Set;

/**
 * The actor ids a use case save names, carried from the request to {@link UseCaseService} beside the converted use
 * case. The DTO converter maps fields only and never turns an id into an artifact, so an actor reference can only be
 * made by the service, through a lookup scoped to the team in the route (OI-46, BR-team-scoped-access).
 *
 * @param primaryActorId    the primary actor's artifact id; null only for a caller inside the server
 * @param secondaryActorIds the secondary actors' artifact ids; a null set is read as empty
 */
public record UseCaseActorIds(Long primaryActorId, Set<Long> secondaryActorIds) {

    public UseCaseActorIds {
        secondaryActorIds = secondaryActorIds == null ? Set.of() : Set.copyOf(secondaryActorIds);
    }
}
