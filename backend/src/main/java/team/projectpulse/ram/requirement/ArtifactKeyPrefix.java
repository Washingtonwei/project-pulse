package team.projectpulse.ram.requirement;

import java.util.EnumMap;
import java.util.Map;

public final class ArtifactKeyPrefix {

    private static final Map<RequirementArtifactType, String> MAP = new EnumMap<>(RequirementArtifactType.class);

    static {
        MAP.put(RequirementArtifactType.GLOSSARY_TERM, "GLO");
        MAP.put(RequirementArtifactType.FUNCTIONAL_REQUIREMENT, "FR");
        MAP.put(RequirementArtifactType.USE_CASE, "UC");
        MAP.put(RequirementArtifactType.BUSINESS_OBJECTIVE, "BO");

        // Add more if you want predictable keys:
        MAP.put(RequirementArtifactType.BUSINESS_PROBLEM, "BP");
        MAP.put(RequirementArtifactType.BUSINESS_OPPORTUNITY, "BOP");
        MAP.put(RequirementArtifactType.SUCCESS_METRIC, "SM");
        MAP.put(RequirementArtifactType.RISK, "RSK");
        MAP.put(RequirementArtifactType.BUSINESS_ASSUMPTION, "BAS");
        MAP.put(RequirementArtifactType.BUSINESS_DEPENDENCY, "BDE");
        MAP.put(RequirementArtifactType.ASSUMPTION, "ASM");
        MAP.put(RequirementArtifactType.DEPENDENCY, "DEP");
        MAP.put(RequirementArtifactType.STAKEHOLDER, "STK");
        // SYS rather than EXT, which would read as "extension" inside a use case.
        MAP.put(RequirementArtifactType.EXTERNAL_SYSTEM, "SYS");
        MAP.put(RequirementArtifactType.BUSINESS_RULE, "RULE");
        MAP.put(RequirementArtifactType.FEATURE, "FEAT");
        // PRE and POST: a key's slug starts with a letter, so PRE-student-logged-in can never be mistaken for the PRE-1
        // or POST-2 a use case gives its own conditions by position (doc.md, decision "Condition key prefixes").
        MAP.put(RequirementArtifactType.PRECONDITION, "PRE");
        MAP.put(RequirementArtifactType.POSTCONDITION, "POST");
        MAP.put(RequirementArtifactType.USER_STORY, "US");
        MAP.put(RequirementArtifactType.QUALITY_ATTRIBUTE, "QA");
        // One prefix per interface kind, matching the UI-/SI-/HI-/CI- keys the SRS uses for its own interfaces.
        MAP.put(RequirementArtifactType.USER_INTERFACE, "UI");
        MAP.put(RequirementArtifactType.SOFTWARE_INTERFACE, "SI");
        MAP.put(RequirementArtifactType.HARDWARE_INTERFACE, "HI");
        MAP.put(RequirementArtifactType.COMMUNICATIONS_INTERFACE, "CI");
        MAP.put(RequirementArtifactType.OPERATING_ENVIRONMENT, "OE");
        MAP.put(RequirementArtifactType.CONSTRAINT, "CON");
        MAP.put(RequirementArtifactType.DATA_REQUIREMENT, "DR");
    }


    private ArtifactKeyPrefix() {
    }

    public static String of(RequirementArtifactType type) {
        return MAP.getOrDefault(type, "UNK");
    }

}