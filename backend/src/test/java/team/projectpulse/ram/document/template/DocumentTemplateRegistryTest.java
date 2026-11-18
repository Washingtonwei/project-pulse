package team.projectpulse.ram.document.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.requirement.SectionType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The template registry's admitted types (BR-artifact-home) and its startup check (docs/design/art.md, Tests: "Registry
 * startup check").
 */
class DocumentTemplateRegistryTest {

    private DocumentTemplateRegistry registry;

    @BeforeEach
    void setUp() {
        this.registry = new DocumentTemplateRegistry();
        this.registry.loadAllTemplates();
    }

    @Test
    void admittedTypes_ListSection_InTemplateOrder() {
        assertThat(this.registry.admittedTypes(DocumentType.SRS, "ASSUMPTIONS_DEPENDENCIES"))
                .containsExactly(RequirementArtifactType.ASSUMPTION, RequirementArtifactType.DEPENDENCY);
    }

    @Test
    void admittedTypes_RichTextSectionOrUnknownKey_Empty() {
        assertThat(this.registry.admittedTypes(DocumentType.SRS, "INTRODUCTION")).isEmpty();
        assertThat(this.registry.admittedTypes(DocumentType.SRS, "NO_SUCH_SECTION")).isEmpty();
    }

    /**
     * Every LIST section admits exactly the types the SRS's "Artifact type → authoring home" table homes there, and
     * every other section admits none (BR-artifact-home; docs/design/art.md, Admitted types). The expected map below is
     * that table, keyed by template and section key; a template change that drifts from it fails here.
     */
    @Test
    void templates_MatchTheAuthoringHomesTable() {
        Map<DocumentType, Map<String, List<RequirementArtifactType>>> homes = new EnumMap<>(DocumentType.class);
        homes.put(DocumentType.VISION_SCOPE, Map.of(
                "BUSINESS_OPPORTUNITY", List.of(RequirementArtifactType.BUSINESS_PROBLEM, RequirementArtifactType.BUSINESS_OPPORTUNITY),
                "BUSINESS_OBJECTIVES", List.of(RequirementArtifactType.BUSINESS_OBJECTIVE),
                "SUCCESS_METRICS", List.of(RequirementArtifactType.SUCCESS_METRIC),
                "RISKS", List.of(RequirementArtifactType.RISK),
                "ASSUMPTIONS_DEPENDENCIES", List.of(RequirementArtifactType.ASSUMPTION, RequirementArtifactType.DEPENDENCY),
                "STAKEHOLDER_PROFILES", List.of(RequirementArtifactType.STAKEHOLDER),
                "EXTERNAL_SYSTEMS", List.of(RequirementArtifactType.EXTERNAL_SYSTEM),
                "MAJOR_FEATURES", List.of(RequirementArtifactType.FEATURE)));
        homes.put(DocumentType.SRS, Map.of(
                "OPERATING_ENVIRONMENT", List.of(RequirementArtifactType.OPERATING_ENVIRONMENT),
                "DESIGN_CONSTRAINTS", List.of(RequirementArtifactType.CONSTRAINT),
                "ASSUMPTIONS_DEPENDENCIES", List.of(RequirementArtifactType.ASSUMPTION, RequirementArtifactType.DEPENDENCY),
                "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS", List.of(RequirementArtifactType.FUNCTIONAL_REQUIREMENT),
                "DATA_REQUIREMENTS", List.of(RequirementArtifactType.DATA_REQUIREMENT),
                "USER_INTERFACES", List.of(RequirementArtifactType.USER_INTERFACE),
                "SOFTWARE_INTERFACES", List.of(RequirementArtifactType.SOFTWARE_INTERFACE),
                "HARDWARE_INTERFACES", List.of(RequirementArtifactType.HARDWARE_INTERFACE),
                "COMMUNICATION_INTERFACES", List.of(RequirementArtifactType.COMMUNICATIONS_INTERFACE),
                "QUALITY_ATTRIBUTES", List.of(RequirementArtifactType.QUALITY_ATTRIBUTE)));
        homes.put(DocumentType.BUSINESS_RULES, Map.of(
                "BUSINESS_RULES", List.of(RequirementArtifactType.BUSINESS_RULE)));

        homes.forEach((type, expected) -> {
            DocumentTemplate template = this.registry.find(type).orElseThrow(() -> new AssertionError("No template for " + type));
            Map<String, List<RequirementArtifactType>> actual = new HashMap<>();
            for (DocumentTemplate.SectionTemplate section : template.getSections()) {
                if (section.getType() == SectionType.LIST) {
                    actual.put(section.getSectionKey(), section.getArtifactTypes());
                } else {
                    assertThat(section.getArtifactTypes()).as(type + " " + section.getSectionKey()).isEmpty();
                }
            }
            assertThat(actual).as(type.name()).isEqualTo(expected);
        });
    }

    @Test
    void templates_NewSectionsSitWhereTheHomesTableSays() {
        List<String> visionScope = sectionKeys(DocumentType.VISION_SCOPE);
        assertThat(visionScope.indexOf("SUCCESS_METRICS")).isEqualTo(visionScope.indexOf("BUSINESS_OBJECTIVES") + 1);
        assertThat(visionScope.indexOf("EXTERNAL_SYSTEMS")).isEqualTo(visionScope.indexOf("PRODUCT_PERSPECTIVE") + 1);
    }

    private List<String> sectionKeys(DocumentType type) {
        return this.registry.find(type).orElseThrow().getSections().stream().map(DocumentTemplate.SectionTemplate::getSectionKey).toList();
    }

    @Test
    void checkArtifactTypes_ListSectionWithoutTypes_Refused() {
        DocumentTemplate template = templateWith(section("RISKS", SectionType.LIST, List.of()));
        assertThatThrownBy(() -> DocumentTemplateRegistry.checkArtifactTypes(DocumentType.VISION_SCOPE, template))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RISKS");
    }

    @Test
    void checkArtifactTypes_RichTextSectionWithTypes_Refused() {
        DocumentTemplate template = templateWith(section("INTRODUCTION", SectionType.RICH_TEXT, List.of(RequirementArtifactType.RISK)));
        assertThatThrownBy(() -> DocumentTemplateRegistry.checkArtifactTypes(DocumentType.VISION_SCOPE, template))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INTRODUCTION");
    }

    @Test
    void checkArtifactTypes_ConsistentSections_Accepted() {
        DocumentTemplate template = templateWith(
                section("INTRODUCTION", SectionType.RICH_TEXT, List.of()),
                section("RISKS", SectionType.LIST, List.of(RequirementArtifactType.RISK)));
        DocumentTemplateRegistry.checkArtifactTypes(DocumentType.VISION_SCOPE, template);
    }

    @Test
    void checkArtifactTypes_TypeWithNoListHome_Refused() {
        DocumentTemplate template = templateWith(section("GLOSSARY", SectionType.LIST, List.of(RequirementArtifactType.GLOSSARY_TERM)));
        assertThatThrownBy(() -> DocumentTemplateRegistry.checkArtifactTypes(DocumentType.VISION_SCOPE, template))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GLOSSARY_TERM");
    }

    /**
     * docs/design/art.md, Tests, "Homes": every type with a list home has exactly one home, except ASSUMPTION and
     * DEPENDENCY, which have one in Vision and Scope and one in the SRS; each interface type has its own SRS section.
     */
    @Test
    void homesOf_EachListTypeHasItsHomes() {
        for (RequirementArtifactType type : RequirementArtifactType.values()) {
            List<DocumentTemplateRegistry.Home> homes = this.registry.homesOf(type);
            if (!type.hasListHome()) {
                assertThat(homes).as(type.name()).isEmpty();
            } else if (type == RequirementArtifactType.ASSUMPTION || type == RequirementArtifactType.DEPENDENCY) {
                assertThat(homes).as(type.name()).containsExactlyInAnyOrder(
                        new DocumentTemplateRegistry.Home(DocumentType.VISION_SCOPE, "ASSUMPTIONS_DEPENDENCIES"),
                        new DocumentTemplateRegistry.Home(DocumentType.SRS, "ASSUMPTIONS_DEPENDENCIES"));
            } else {
                assertThat(homes).as(type.name()).hasSize(1);
            }
        }
        assertThat(this.registry.homesOf(RequirementArtifactType.USER_INTERFACE)).containsExactly(new DocumentTemplateRegistry.Home(DocumentType.SRS, "USER_INTERFACES"));
        assertThat(this.registry.homesOf(RequirementArtifactType.SOFTWARE_INTERFACE)).containsExactly(new DocumentTemplateRegistry.Home(DocumentType.SRS, "SOFTWARE_INTERFACES"));
        assertThat(this.registry.homesOf(RequirementArtifactType.HARDWARE_INTERFACE)).containsExactly(new DocumentTemplateRegistry.Home(DocumentType.SRS, "HARDWARE_INTERFACES"));
        assertThat(this.registry.homesOf(RequirementArtifactType.COMMUNICATIONS_INTERFACE)).containsExactly(new DocumentTemplateRegistry.Home(DocumentType.SRS, "COMMUNICATION_INTERFACES"));
    }

    @Test
    void checkHomes_HomelessListType_Refused() {
        Map<DocumentType, DocumentTemplate> templates = new EnumMap<>(DocumentType.class);
        templates.put(DocumentType.VISION_SCOPE, templateWith(section("RISKS", SectionType.LIST, List.of(RequirementArtifactType.RISK))));
        assertThatThrownBy(() -> DocumentTemplateRegistry.checkHomes(templates))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("0 list home(s)");
    }

    @Test
    void checkHomes_SecondHomeForAOneHomeType_Refused() {
        Map<DocumentType, DocumentTemplate> templates = new EnumMap<>(DocumentType.class);
        templates.putAll(Map.of(
                DocumentType.VISION_SCOPE, this.registry.find(DocumentType.VISION_SCOPE).orElseThrow(),
                DocumentType.BUSINESS_RULES, this.registry.find(DocumentType.BUSINESS_RULES).orElseThrow(),
                DocumentType.SRS, templateWith(concat(this.registry.find(DocumentType.SRS).orElseThrow().getSections(),
                        section("MORE_RISKS", SectionType.LIST, List.of(RequirementArtifactType.RISK))))));
        assertThatThrownBy(() -> DocumentTemplateRegistry.checkHomes(templates))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RISK has 2 list home(s)");
    }

    @Test
    void checkHomes_TheShippedTemplates_Accepted() {
        Map<DocumentType, DocumentTemplate> templates = new EnumMap<>(DocumentType.class);
        for (DocumentType type : DocumentType.values()) {
            this.registry.find(type).ifPresent(template -> templates.put(type, template));
        }
        DocumentTemplateRegistry.checkHomes(templates);
    }

    private static DocumentTemplate.SectionTemplate[] concat(List<DocumentTemplate.SectionTemplate> sections, DocumentTemplate.SectionTemplate extra) {
        java.util.List<DocumentTemplate.SectionTemplate> all = new java.util.ArrayList<>(sections);
        all.add(extra);
        return all.toArray(new DocumentTemplate.SectionTemplate[0]);
    }

    private static DocumentTemplate templateWith(DocumentTemplate.SectionTemplate... sections) {
        DocumentTemplate template = new DocumentTemplate();
        template.setSections(List.of(sections));
        return template;
    }

    private static DocumentTemplate.SectionTemplate section(String key, SectionType type, List<RequirementArtifactType> types) {
        DocumentTemplate.SectionTemplate section = new DocumentTemplate.SectionTemplate();
        section.setSectionKey(key);
        section.setType(type);
        section.setArtifactTypes(types);
        return section;
    }
}
