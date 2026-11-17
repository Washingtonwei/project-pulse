package team.projectpulse.ram.document.template;

import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.requirement.SectionType;

import java.util.ArrayList;
import java.util.List;

public class DocumentTemplate {

    private String title;
    private List<SectionTemplate> sections = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SectionTemplate> getSections() {
        return sections;
    }

    public void setSections(List<SectionTemplate> sections) {
        this.sections = sections;
    }

    public static class SectionTemplate {
        private String sectionKey;
        private String title;
        private SectionType type;
        private String guidance;
        // The artifact types a LIST section holds (BR-artifact-home), in the order the editor offers them; empty for a
        // RICH_TEXT section. DocumentTemplateRegistry refuses a template that breaks either half at startup.
        private List<RequirementArtifactType> artifactTypes = List.of();

        public String getSectionKey() {
            return sectionKey;
        }

        public void setSectionKey(String sectionKey) {
            this.sectionKey = sectionKey;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public SectionType getType() {
            return type;
        }

        public void setType(SectionType type) {
            this.type = type;
        }

        public String getGuidance() {
            return guidance;
        }

        public void setGuidance(String guidance) {
            this.guidance = guidance;
        }

        public List<RequirementArtifactType> getArtifactTypes() {
            return artifactTypes;
        }

        public void setArtifactTypes(List<RequirementArtifactType> artifactTypes) {
            this.artifactTypes = artifactTypes != null ? List.copyOf(artifactTypes) : List.of();
        }
    }
}
