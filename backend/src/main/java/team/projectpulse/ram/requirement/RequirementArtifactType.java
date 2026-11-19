package team.projectpulse.ram.requirement;

/**
 * The product's taxonomy of requirement artifacts (the glossary's requirement artifact list; the SRS's Business Domain
 * Model). Every type but five is an item of a list-type document section, its home (BR-artifact-home); see
 * {@link #hasListHome()}.
 */
public enum RequirementArtifactType {
    GLOSSARY_TERM,
    BUSINESS_PROBLEM,
    BUSINESS_OPPORTUNITY,
    BUSINESS_OBJECTIVE,
    SUCCESS_METRIC,
    RISK,
    // Assumptions and dependencies come in two kinds, one per document, so each type has one home (BR-artifact-home):
    // the business-level ones in the Vision and Scope, the system-level ones in the SRS.
    BUSINESS_ASSUMPTION,
    BUSINESS_DEPENDENCY,
    ASSUMPTION,
    DEPENDENCY,
    STAKEHOLDER,
    EXTERNAL_SYSTEM,
    BUSINESS_RULE,
    FUNCTIONAL_REQUIREMENT,
    FEATURE,
    USE_CASE,
    PRECONDITION,
    POSTCONDITION,
    USER_STORY,
    QUALITY_ATTRIBUTE,
    USER_INTERFACE,
    SOFTWARE_INTERFACE,
    HARDWARE_INTERFACE,
    COMMUNICATIONS_INTERFACE,
    OPERATING_ENVIRONMENT,
    CONSTRAINT,
    DATA_REQUIREMENT;

    /**
     * Whether artifacts of this type are items of a list-type document section. False for glossary terms (authored in
     * the glossary), use cases, preconditions and postconditions (authored in the use case editor), and user stories
     * (deferred); true for every other type. This is the one place that says so: the template registry checks the
     * templates against it at startup, and the create endpoint refuses a type without a list home.
     */
    public boolean hasListHome() {
        return switch (this) {
            case GLOSSARY_TERM, USE_CASE, PRECONDITION, POSTCONDITION, USER_STORY -> false;
            default -> true;
        };
    }
}
