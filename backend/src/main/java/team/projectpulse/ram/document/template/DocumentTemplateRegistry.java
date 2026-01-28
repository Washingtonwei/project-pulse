package team.projectpulse.ram.document.template;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentType;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
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

        } catch (IOException e) {
            log.error("Failed to scan for template files", e);
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
            log.error("Failed to load template from {}", filename, e);
        }
    }

    private void loadTemplate(Resource resource, DocumentType type) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            DocumentTemplate template = yamlMapper.readValue(in, DocumentTemplate.class);
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

    public Optional<DocumentTemplate> find(DocumentType type) {
        return Optional.ofNullable(templates.get(type));
    }

}