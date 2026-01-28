package team.projectpulse.ram.requirement;

import org.springframework.data.jpa.domain.Specification;

public class ArtifactLinkSpecs {

    public static Specification<ArtifactLink> hasType(String type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<ArtifactLink> hasTeamId(Integer teamId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("team").get("id"), teamId);
    }

}
