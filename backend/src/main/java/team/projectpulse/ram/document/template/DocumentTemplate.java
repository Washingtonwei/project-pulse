package team.projectpulse.ram.document.template;

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
    }
}
