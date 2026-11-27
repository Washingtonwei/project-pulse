package team.projectpulse.ram.document.template;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.requirement.SectionType;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DocumentTemplateRegistry {

    private static final Logger log = LoggerFactory.getLogger(DocumentTemplateRegistry.class);
    private static final String TEMPLATE_PATH = "classpath:requirement-document-templates/*.yml";
    private static final String YML_EXTENSION = ".yml";

    private final YAMLMapper yamlMapper = YAMLMapper.builder()
            .findAndAddModules()
            .build();

    private final Map<DocumentType, DocumentTemplate> templates = new EnumMap<>(DocumentType.class);

    // Each list type's one home (BR-artifact-home), built once at startup by buildHomes.
    private final Map<RequirementArtifactType, Home> homes = new EnumMap<>(RequirementArtifactType.class);

    /**
     * The home of an artifact type: the list section with {@code sectionKey} in a {@code documentType} document.
     */
    public record Home(DocumentType documentType, String sectionKey) {
    }

    /**
     * Loads all YAML template files from src/main/resources/requirement-document-templates/
     * Each file name must match enum values defined in DocumentType.
     * Example: VISION_SCOPE.yml, SRS.yml
     */
    @PostConstruct
    public void loadAllTemplates() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(TEMPLATE_PATH);

            log.info("Scanning for document templates, found {} file(s)", resources.length);

            for (Resource resource : resources) {
                processTemplateFile(resource);
            }

            log.info("Successfully loaded {} document template(s)", templates.size());
            validateTemplates();
            homes.putAll(buildHomes(templates));

        } catch (IOException e) {
            // Every document is created from its template (UC-TPL-provision-documents 4a), so templates that cannot be
            // read stop startup here rather than surfacing later as a 400 that blames a missing template
            throw new IllegalStateException("Failed to scan for template files.", e);
        }
    }

    private void processTemplateFile(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null || !filename.endsWith(YML_EXTENSION)) {
            return;
        }

        String typeName = filename.substring(0, filename.length() - YML_EXTENSION.length());

        try {
            DocumentType type = DocumentType.valueOf(typeName);
            loadTemplate(resource, type);
        } catch (IllegalArgumentException e) {
            log.warn("Skipping unknown template file: {} (not a valid DocumentType)", filename);
        } catch (IOException e) {
            // A file that cannot be read stops startup. A malformed one already does: Jackson 3's parse errors are
            // unchecked, so they never reach this catch.
            throw new IllegalStateException("Failed to load template from " + filename + ".", e);
        }
    }

    private void loadTemplate(Resource resource, DocumentType type) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            DocumentTemplate template = yamlMapper.readValue(in, DocumentTemplate.class);
            checkArtifactTypes(type, template);
            templates.put(type, template);
            log.debug("Loaded template for DocumentType: {}", type);
        }
    }

    private void validateTemplates() {
        for (DocumentType type : DocumentType.values()) {
            if (!templates.containsKey(type)) {
                log.warn("No template found for DocumentType: {}", type);
            }
        }
    }

    /**
     * Refuses a template whose sections disagree with their type: a LIST section must name the artifact types it
     * holds, and a RICH_TEXT section must name none. Thrown from {@link #loadAllTemplates}, so a broken template
     * stops the application at startup instead of leaving a list that admits nothing (BR-artifact-home).
     */
    static void checkArtifactTypes(DocumentType type, DocumentTemplate template) {
        for (DocumentTemplate.SectionTemplate section : template.getSections()) {
            boolean hasTypes = !section.getArtifactTypes().isEmpty();
            if (section.getType() == SectionType.LIST && !hasTypes) {
                throw new IllegalStateException("Template " + type + ": LIST section " + section.getSectionKey() + " names no artifactTypes.");
            }
            if (section.getType() == SectionType.RICH_TEXT && hasTypes) {
                throw new IllegalStateException("Template " + type + ": RICH_TEXT section " + section.getSectionKey() + " names artifactTypes.");
            }
            for (RequirementArtifactType artifactType : section.getArtifactTypes()) {
                if (!artifactType.hasListHome()) {
                    throw new IllegalStateException("Template " + type + ": section " + section.getSectionKey() + " admits " + artifactType + ", which has no list home.");
                }
            }
        }
    }

    /**
     * Maps each list type to its one home (BR-artifact-home): the list section whose template admits it, for example
     * {@code RISK} -> (VISION_SCOPE, "RISKS"). Run once at startup, after every template is loaded. The templates are
     * YAML data and can break the rule, so a type admitted by a second section, or a list type admitted by none, is
     * refused here and stops the application, instead of making creates fail or land in the wrong list. Static, taking
     * the templates as an argument, so the unit tests can pass hand-built templates with a missing or extra home.
     */
    static Map<RequirementArtifactType, Home> buildHomes(Map<DocumentType, DocumentTemplate> templates) {
        Map<RequirementArtifactType, Home> homes = new EnumMap<>(RequirementArtifactType.class);
        templates.forEach((documentType, template) -> {
            for (DocumentTemplate.SectionTemplate section : template.getSections()) {
                for (RequirementArtifactType artifactType : section.getArtifactTypes()) {
                    Home home = new Home(documentType, section.getSectionKey());
                    Home earlier = homes.putIfAbsent(artifactType, home);
                    if (earlier != null) {
                        throw new IllegalStateException("Artifact type " + artifactType + " has two homes in the templates: " + earlier + " and " + home + ".");
                    }
                }
            }
        });
        for (RequirementArtifactType artifactType : RequirementArtifactType.values()) {
            if (artifactType.hasListHome() && !homes.containsKey(artifactType)) {
                throw new IllegalStateException("Artifact type " + artifactType + " has no home in the templates.");
            }
        }
        return homes;
    }

    /**
     * The home of {@code artifactType}, for example {@code RISK} -> (VISION_SCOPE, "RISKS"). Empty for a type without
     * a list home; {@link #buildHomes} guarantees one for every other type.
     */
    public Optional<Home> homeOf(RequirementArtifactType artifactType) {
        return Optional.ofNullable(this.homes.get(artifactType));
    }

    public Optional<DocumentTemplate> find(DocumentType type) {
        return Optional.ofNullable(templates.get(type));
    }

    /**
     * The artifact types a document section of a {@code type} document holds, in template order (BR-artifact-home).
     * Empty for a RICH_TEXT section, and for a section key the template no longer has.
     */
    public List<RequirementArtifactType> admittedTypes(DocumentType type, String sectionKey) {
        return find(type).stream()
                .flatMap(template -> template.getSections().stream())
                .filter(section -> section.getSectionKey().equals(sectionKey))
                .findFirst()
                .map(DocumentTemplate.SectionTemplate::getArtifactTypes)
                .map(List::copyOf)
                .orElse(List.of());
    }

}