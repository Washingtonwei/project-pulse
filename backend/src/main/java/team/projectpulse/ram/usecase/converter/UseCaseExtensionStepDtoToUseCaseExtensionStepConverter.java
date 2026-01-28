package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseExtensionStep;
import team.projectpulse.ram.usecase.dto.UseCaseExtensionStepDto;

@Component
public class UseCaseExtensionStepDtoToUseCaseExtensionStepConverter implements Converter<UseCaseExtensionStepDto, UseCaseExtensionStep> {
    @Override
    public UseCaseExtensionStep convert(UseCaseExtensionStepDto source) {
        return new UseCaseExtensionStep(
                source.id(),
                source.actor(),
                source.actionText()
        );
    }
}
