package team.projectpulse.ram.document.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.requirement.SectionType;

import java.util.List;

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
