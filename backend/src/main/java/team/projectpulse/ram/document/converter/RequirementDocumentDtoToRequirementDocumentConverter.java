package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.dto.RequirementDocumentDto;

@Component
public class RequirementDocumentDtoToRequirementDocumentConverter implements Converter<RequirementDocumentDto, RequirementDocument> {
    @Override
    public RequirementDocument convert(RequirementDocumentDto source) {
        RequirementDocument document = new RequirementDocument();
        document.setId(source.id());
        document.setType(source.type());
        document.setDocumentKey(source.documentKey());
        document.setTitle(source.title());
        document.setStatus(source.status());
        return document;
    }
}
