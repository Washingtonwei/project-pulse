package team.projectpulse.ram.usecase;

import java.util.List;
import java.util.Set;

/**
 * The references a use case save names, carried from the request to {@link UseCaseService} beside the converted use
 * case: its actors, by id, and its preconditions and postconditions, in order. The DTO converter maps fields only and
 * never turns an id into an artifact, so a reference can only be made by the service, through a lookup scoped to the
 * team in the route (OI-46, BR-team-scoped-access).
 * <p>
 * None of the collections may be null; a null is refused, never read as empty.
 *
 * @param primaryActorId    the primary actor's artifact id; required, and refused by the service when null
 * @param secondaryActorIds the secondary actors' artifact ids; required, empty for none
 * @param preconditions     the preconditions in the use case's order; required, empty for none
 * @param postconditions    the postconditions in the use case's order; required, empty for none
 */
public record UseCaseReferences(Long primaryActorId, Set<Long> secondaryActorIds,
                                List<SubmittedCondition> preconditions, List<SubmittedCondition> postconditions) {

    public UseCaseReferences {
        // Set.copyOf and List.copyOf throw a NullPointerException on a null, and that is deliberate: a missing
        // collection must not become an empty one, because on an edit an empty one unlinks every actor or condition
        // it held (doc.md, "A list missing from the body"). @NotNull on UseCaseDto already turns a missing list in a
        // request into a 400, so this can only fail for a caller inside the server that passed null by mistake.
        secondaryActorIds = Set.copyOf(secondaryActorIds);
        preconditions = List.copyOf(preconditions);
        postconditions = List.copyOf(postconditions);
    }
}
