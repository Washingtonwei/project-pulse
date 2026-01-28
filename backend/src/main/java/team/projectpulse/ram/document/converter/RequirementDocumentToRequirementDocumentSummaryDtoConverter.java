package team.projectpulse.ram.document.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.document.dto.RequirementDocumentSummaryDto;

@Component
public class RequirementDocumentToRequirementDocumentSummaryDtoConverter implements Converter<RequirementDocument, RequirementDocumentSummaryDto> {

    @Override
    public RequirementDocumentSummaryDto convert(RequirementDocument source) {
        return new RequirementDocumentSummaryDto(
                source.getId(),
                source.getType(),
                source.getTeam().getTeamId(),
                source.getDocumentKey(),
                source.getTitle(),
                source.getStatus(),
                source.getVersion()
        );
    }
}
