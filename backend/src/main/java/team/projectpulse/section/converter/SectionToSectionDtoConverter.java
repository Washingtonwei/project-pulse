package team.projectpulse.section.converter;

import team.projectpulse.section.Section;
import team.projectpulse.section.dto.SectionDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class SectionToSectionDtoConverter implements Converter<Section, SectionDto> {

    @Override
    public SectionDto convert(Section section) {
        return new SectionDto(
                section.getSectionId(),
                section.getSectionName(),
                section.getStartDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                section.getEndDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                section.getRubric() == null ? null : section.getRubric().getRubricId(),
                section.getRubric() == null ? null : section.getRubric().getRubricName(),
                section.getActiveWeeks(),
                section.getCourse().getCourseId(),
                section.isActive(),
                section.getWarWeeklyDueDay(),
                section.getWarDueTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                section.getPeerEvaluationWeeklyDueDay(),
                section.getPeerEvaluationDueTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }

}
