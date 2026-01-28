package team.projectpulse.ram.requirement;

import org.springframework.data.jpa.domain.Specification;

public class RequirementArtifactSpecs {

    public static Specification<RequirementArtifact> hasType(RequirementArtifactType type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<RequirementArtifact> hasArtifactKeyLike(String artifactKey) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("artifactKey"), "%" + artifactKey + "%");
    }

    public static Specification<RequirementArtifact> hasTitleLike(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("title"), "%" + title + "%");
    }

    public static Specification<RequirementArtifact> hasContentLike(String content) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("content"), "%" + content + "%");
    }

    public static Specification<RequirementArtifact> hasPriority(Priority priority) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<RequirementArtifact> hasTeamId(Integer teamId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("team").get("id"), teamId);
    }

}
