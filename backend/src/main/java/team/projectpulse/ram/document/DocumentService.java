package team.projectpulse.ram.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import team.projectpulse.ram.document.template.DocumentTemplate;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.ArrayList;
import java.util.Map;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TeamRepository teamRepository;
    private final DocumentTemplateRegistry documentTemplateRegistry;


    public DocumentService(DocumentRepository documentRepository, TeamRepository teamRepository, DocumentTemplateRegistry documentTemplateRegistry) {
        this.documentRepository = documentRepository;
        this.teamRepository = teamRepository;
        this.documentTemplateRegistry = documentTemplateRegistry;
    }

    public Page<RequirementDocument> findByCriteria(Integer teamId, Map<String, String> searchCriteria, Pageable pageable) {
        Specification<RequirementDocument> spec = Specification.unrestricted(); // Start with an unrestricted specification

        if (StringUtils.hasLength(searchCriteria.get("type"))) {
            spec = spec.and(RequirementDocumentSpecs.hasType(DocumentType.valueOf(searchCriteria.get("type"))));
        }

        if (StringUtils.hasLength(searchCriteria.get("title"))) {
            spec = spec.and(RequirementDocumentSpecs.hasTitleLike(searchCriteria.get("title")));
        }

        spec = spec.and(RequirementDocumentSpecs.hasTeamId(teamId));

        return this.documentRepository.findAll(spec, pageable);
    }

    /**
     * Finds a requirement document by ID with all related entities fully loaded.
     * Uses the 3-query approach to avoid big Cartesian products:
     * 1. Load requirement document with scalar relationships (team)
     * 2. Load the document sections graph (sections -> requirementArtifacts -> artifactLinks)
     * <p>
     * All queries execute within the same transaction/persistence context,
     * so Hibernate automatically merges them into a single requirement document entity.
     *
     * @param teamId     the team ID
     * @param documentId the document ID
     * @return the fully loaded requirement document
     * @throws ObjectNotFoundException if the requirement document is not found
     */
    public RequirementDocument findDocumentByIdWithFullGraph(Integer teamId, Long documentId) {
        // Query 1: Load RequirementDocument with scalar relationships
        RequirementDocument document = this.documentRepository.findByIdWithScalars(documentId)
                .orElseThrow(() -> new ObjectNotFoundException("document", documentId));

        // Query 2: Load the sections hierarchy
        this.documentRepository.findByIdWithSectionGraph(documentId);

        // Return the fully loaded entity
        // All collections are now populated in the same instance
        return document;
    }

    public RequirementDocument findDocumentByIdBasic(Integer teamId, Long documentId) {
        return this.documentRepository.findByIdWithScalars(documentId)
                .orElseThrow(() -> new ObjectNotFoundException("document", documentId));
    }

    public RequirementDocument createRequirementDocument(Integer teamId, DocumentType type) {
        Team team = this.teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("team", teamId));

        // We need to check if a document with the same type already exists for the team
        this.documentRepository.findByTeamTeamIdAndType(teamId, type)
                .ifPresent(doc -> {
                    throw new IllegalStateException("A document of type " + type + " already exists for team with ID " + teamId);
                });

        RequirementDocument doc = new RequirementDocument();
        doc.setType(type);
        doc.setTeam(team);
        doc.setDocumentKey(type.name());

        this.documentTemplateRegistry.find(type).ifPresentOrElse(
                tpl -> applyTemplate(doc, tpl),
                () -> doc.setTitle(type.name()) // fallback title if no template file exists
        );

        return this.documentRepository.save(doc);
    }

    private void applyTemplate(RequirementDocument doc, DocumentTemplate tpl) {
        doc.setTitle(tpl.getTitle() != null ? tpl.getTitle() : doc.getType().name());

        if (tpl.getSections() == null || tpl.getSections().isEmpty()) {
            return;
        }

        for (DocumentTemplate.SectionTemplate sectionTemplate : tpl.getSections()) {
            DocumentSection section = new DocumentSection();
            section.setSectionKey(sectionTemplate.getSectionKey());
            section.setTitle(sectionTemplate.getTitle());
            section.setType(sectionTemplate.getType());
            section.setContent(null);
            if (sectionTemplate.getType() == SectionType.LIST) {
                section.setRequirementArtifacts(new ArrayList<>());
            }
            section.setGuidance(sectionTemplate.getGuidance());
            section.initLockIfMissing();

            doc.addSection(section);
        }
    }

    // Only status can be updated for now
    public RequirementDocument updateRequirementDocument(Integer teamId, Long documentId, RequirementDocument update) {
        return this.documentRepository.findById(documentId)
                .map(oldDocument -> {
                    oldDocument.setStatus(update.getStatus());
                    return this.documentRepository.save(oldDocument);
                })
                .orElseThrow(() -> new ObjectNotFoundException("document", documentId));
    }


}
