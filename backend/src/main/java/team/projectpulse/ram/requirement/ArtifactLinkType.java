package team.projectpulse.ram.requirement;

public enum ArtifactLinkType {
    DERIVES_FROM, // FR -> UC -> feature -> objective, low level requirement -> high level requirement
    REALIZES, // implementation -> design -> FR/UC/feature/objective
    REFERENCES,
    IMPACTS, // change/NFR/risk -> requirement/artifact (cross-cutting)
    MITIGATES, // control/req -> risk/threat
    MOTIVATES // stakeholder -> requirement
}
