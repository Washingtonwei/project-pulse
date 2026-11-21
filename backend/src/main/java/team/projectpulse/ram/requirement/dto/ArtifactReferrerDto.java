package team.projectpulse.ram.requirement.dto;

import team.projectpulse.ram.requirement.ArtifactLinkType;

/**
 * One reference to a requirement artifact that blocks its delete (UC-ART-delete-artifact step 4, 4a;
 * BR-deletion-integrity). Every reference is an artifact link, and {@code id}, {@code artifactKey} and {@code title} are
 * those of the artifact at the link's other end. A use case naming an actor is a link from the use case's own artifact,
 * which shares the use case's id.
 * For example, with FR-1 linked to UC-1 and UC-1 naming the stakeholder STK-1 as its primary actor:
 * <pre>
 * references of FR-1:  (3, "UC-1", "Create a use case", LINK_FROM, DERIVES_FROM)   FR-1 is the link's source
 * references of STK-1: (3, "UC-1", "Create a use case", LINK_TO, PRIMARY_ACTOR)
 * </pre>
 *
 * @param linkType the link's type
 */
public record ArtifactReferrerDto(Long id, String artifactKey, String title, Relation relation, ArtifactLinkType linkType) {

    /** How the other artifact refers to the one being deleted. */
    public enum Relation {
        /** The artifact being deleted is the link's source (a link to itself is listed once, this way). */
        LINK_FROM,
        /** The artifact being deleted is the link's target. */
        LINK_TO
    }

}
