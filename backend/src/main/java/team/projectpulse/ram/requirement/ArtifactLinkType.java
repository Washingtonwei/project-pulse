package team.projectpulse.ram.requirement;

public enum ArtifactLinkType {
    CONSTRAINS, // business rule -> use case or FR: the target must conform to the rule (BR-use-case-business-rules)
    DERIVES_FROM, // FR -> UC -> feature -> objective, low level requirement -> high level requirement
    REALIZES, // implementation -> design -> FR/UC/feature/objective
    REFERENCES,
    IMPACTS, // change/NFR/risk -> requirement/artifact (cross-cutting)
    MITIGATES, // control/req -> risk/threat
    MOTIVATES, // stakeholder -> requirement
    ENABLES, // postcondition -> precondition: the outcome of one use case makes another's precondition true
    PRIMARY_ACTOR, // use case -> stakeholder or external system, its one primary actor (BR-use-case-actor-types)
    SECONDARY_ACTOR // use case -> stakeholder or external system, one of its secondary actors
}
