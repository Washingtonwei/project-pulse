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
        Map<Long, T> ownById = new HashMap<>();
        for (T child : own) {
            Long id = idOf.apply(child);
            if (id != null) {
                ownById.put(id, child);
            }
        }

        // Pair. remove() rather than get(), so a repeated id pairs only once and its later copies become new.
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
        Set<T> kept = Collections.newSetFromMap(new IdentityHashMap<>());
        kept.addAll(revised);
        own.removeIf(child -> !kept.contains(child));

        // Insert and move into submission order. After the removal every child left in the list is in `revised`,
        // so once this loop places each of them the list equals `revised`.
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
