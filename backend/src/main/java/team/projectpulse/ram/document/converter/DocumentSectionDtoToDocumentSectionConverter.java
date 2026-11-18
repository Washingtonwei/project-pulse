package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.dto.DocumentSectionDto;

@Component
public class DocumentSectionDtoToDocumentSectionConverter implements Converter<DocumentSectionDto, DocumentSection> {

    @Override
    public DocumentSection convert(DocumentSectionDto source) {
        DocumentSection documentSection = new DocumentSection();
        documentSection.setId(source.id());
        documentSection.setSectionKey(source.sectionKey());
        documentSection.setTitle(source.title());
        documentSection.setType(source.type());
        documentSection.setContent(source.content());
        documentSection.setGuidance(source.guidance());
        return documentSection;
    }
}
