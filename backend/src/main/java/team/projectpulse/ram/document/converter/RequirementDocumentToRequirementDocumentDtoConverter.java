package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.dto.RequirementDocumentDto;

@Component
public class RequirementDocumentToRequirementDocumentDtoConverter implements Converter<RequirementDocument, RequirementDocumentDto> {

    private final DocumentSectionToDocumentSectionDtoConverter documentSectionToDocumentSectionDtoConverter;


    public RequirementDocumentToRequirementDocumentDtoConverter(DocumentSectionToDocumentSectionDtoConverter documentSectionToDocumentSectionDtoConverter) {
        this.documentSectionToDocumentSectionDtoConverter = documentSectionToDocumentSectionDtoConverter;
    }

    @Override
    public RequirementDocumentDto convert(RequirementDocument source) {
        return new RequirementDocumentDto(
                source.getId(),
                source.getType(),
                source.getTeam().getTeamId(),
                source.getDocumentKey(),
                source.getTitle(),
                source.getSections().stream().map(this.documentSectionToDocumentSectionDtoConverter::convert).toList(),
                source.getStatus(),
                source.getVersion()
        );
    }
}
