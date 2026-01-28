package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseExtensionStep;
import team.projectpulse.ram.usecase.dto.UseCaseExtensionStepDto;

@Component
public class UseCaseExtensionStepToUseCaseExtensionStepDtoConverter implements Converter<UseCaseExtensionStep, UseCaseExtensionStepDto> {
    @Override
    public UseCaseExtensionStepDto convert(UseCaseExtensionStep source) {
        return new UseCaseExtensionStepDto(
                source.getId(),
                source.getActor(),
                source.getActionText()
        );
    }
}
