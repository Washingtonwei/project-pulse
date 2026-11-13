package team.projectpulse.ram.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Revises an ordered child list of the use-case aggregate (main steps, a step's extensions, or an extension's
 * steps) so that it matches a submitted list, editing the managed list in place.
 *
 * <p>Each submitted child is paired with an existing child <em>of this same parent</em> by id. A paired child is
 * updated in place and keeps its id. A submitted child that pairs with nothing (no id, an id this parent does not
 * own, or an id already used earlier in the submission) becomes a fresh copy without an id. The submitted object
 * itself is never attached, so an id taken from a request body can only ever select among this parent's own rows;
 * it is never looked up anywhere else (OI-46).
 *
 * <p>The managed list is then edited with explicit operations rather than cleared and refilled: children the
 * submission no longer lists are removed (orphan removal deletes them and cascades to their own children), and
 * the rest are inserted or moved into submission order. Children already in place are not touched. The parent's
 * {@code @OrderColumn} then numbers the positions.
 *
 * <p>The comments inside {@link #revise} follow one worked example through every phase.
 */
final class OwnedChildren {

    private OwnedChildren() {
    }

    /**
     * @param own           the parent's managed list, edited in place
     * @param submitted     the desired children, in order; their ids are pairing keys only
     * @param idOf          reads a child's id
     * @param updateInPlace copies a submitted child's content onto the paired existing child
     * @param freshCopy     builds a new child, without an id, from a submitted child
     * @param attach        points a child's back-reference at the parent
     */
    static <T> void revise(List<T> own, List<T> submitted, Function<T, Long> idOf,
                           BiConsumer<T, T> updateInPlace, UnaryOperator<T> freshCopy, Consumer<T> attach) {
        // Worked example. A use case has the main steps [A, B, C], with ids 1, 2 and 3. In the editor the user
        // deletes B, moves C to the top, and adds a new step right after it. The editor submits [C', X, A']:
        //   C' carries id 3, X has no id, and A' carries id 1.
        // The primes mark the submitted objects. They are detached objects built by the converters from the
        // request body, never the managed rows A and C themselves.
        // Expected result: own = [C, N, A]. C and A are the same managed rows with their old ids, B is deleted,
        // and N is a brand-new row built from X.

        // Index this parent's own children by id. Example: ownById = {1: A, 2: B, 3: C}.
        Map<Long, T> ownById = new HashMap<>();
        for (T child : own) {
            Long id = idOf.apply(child);
            if (id != null) {
                ownById.put(id, child);
            }
        }

        // Pair. remove() rather than get(), so a repeated id pairs only once and its later copies become new.
        // Example, one submitted child at a time:
        //   C' (id 3): ownById.remove(3) returns C. C is updated from C'.      revised = [C]
        //   X (no id): nothing to pair with. N is built as a fresh copy of X.  revised = [C, N]
        //   A' (id 1): ownById.remove(1) returns A. A is updated from A'.      revised = [C, N, A]
        // Id 2 was never submitted, so B stays unpaired (ownById = {2: B}). Had the submission listed id 3 a second
        // time, ownById.remove(3) would then return null, and that second copy would become a fresh row too. An id
        // from another use case or another team is never in ownById at all, so it can only ever produce a fresh row.
        List<T> revised = new ArrayList<>();
        if (submitted != null) {
            for (T candidate : submitted) {
                Long id = idOf.apply(candidate);
                T paired = id == null ? null : ownById.remove(id);
                if (paired == null) {
                    revised.add(freshCopy.apply(candidate));
                } else {
                    updateInPlace.accept(paired, candidate);
                    revised.add(paired);
                }
            }
        }

        // Delete the children the submission no longer lists. The entities do not override equals, so membership
        // is by identity; the identity set says so explicitly.
        // Example: kept = {C, N, A}. B is not in it, so own goes from [A, B, C] to [A, C]. At flush, orphan removal
        // deletes B's row along with B's extensions and their steps. N is not in own yet; the next phase places it.
        Set<T> kept = Collections.newSetFromMap(new IdentityHashMap<>());
        kept.addAll(revised);
        own.removeIf(child -> !kept.contains(child));

        // Insert and move into submission order. After the removal every child left in the list is in `revised`,
        // so once this loop places each of them the list equals `revised`.
        // Each pass makes position i hold revised[i]. Positions before i are already final, so the loop never
        // disturbs them.
        // Example, one position at a time, starting from own = [A, C]:
        //   i = 0: wants C, finds A. remove(C) returns true, so this is a move.   own = [A] then [C, A]
        //   i = 1: wants N, finds A. remove(N) returns false, so this is an insert, and N is attached to the
        //          parent.                                                       own = [C, N, A]
        //   i = 2: wants A, finds A. Already in place, so nothing happens.
        // own now equals revised, [C, N, A]. At flush Hibernate inserts N, deletes B, writes the new text onto C
        // and A, and the @OrderColumn writes the positions 0, 1 and 2.
        for (int i = 0; i < revised.size(); i++) {
            T wanted = revised.get(i);
            if (i < own.size() && own.get(i) == wanted) { // NOPMD - identity on purpose: same managed instance, already in place
                continue;
            }
            boolean moved = own.remove(wanted); // true for an existing child being moved, false for a fresh copy
            own.add(i, wanted);
            if (!moved) {
                attach.accept(wanted); // only a fresh copy needs its back-reference set; moving never re-parents
            }
        }
    }

}
