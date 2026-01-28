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
        MAP.put(RequirementArtifactType.VISION_STATEMENT, "VS");
        MAP.put(RequirementArtifactType.BUSINESS_RISK, "BR");
        MAP.put(RequirementArtifactType.ASSUMPTION, "ASM");
        MAP.put(RequirementArtifactType.RISK, "RSK");
        MAP.put(RequirementArtifactType.STAKEHOLDER, "STK");
        MAP.put(RequirementArtifactType.BUSINESS_RULE, "RULE");
        MAP.put(RequirementArtifactType.FEATURE, "FEAT");
        MAP.put(RequirementArtifactType.PRECONDITION, "PRE");
        MAP.put(RequirementArtifactType.POSTCONDITION, "POST");
        MAP.put(RequirementArtifactType.USER_STORY, "US");
        MAP.put(RequirementArtifactType.QUALITY_ATTRIBUTE, "QA");
        MAP.put(RequirementArtifactType.EXTERNAL_INTERFACE_REQUIREMENT, "EIR");
        MAP.put(RequirementArtifactType.CONSTRAINT, "CON");
        MAP.put(RequirementArtifactType.DATA_REQUIREMENT, "DR");
        MAP.put(RequirementArtifactType.OTHER, "OTH");
    }


    private ArtifactKeyPrefix() {
    }

    public static String of(RequirementArtifactType type) {
        return MAP.getOrDefault(type, "UNK");
    }

}