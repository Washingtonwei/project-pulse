package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseMainStep;
import team.projectpulse.ram.usecase.dto.UseCaseMainStepDto;

@Component
public class UseCaseMainStepDtoToUseCaseMainStepConverter implements Converter<UseCaseMainStepDto, UseCaseMainStep> {

    private final UseCaseExtensionDtoToUseCaseExtensionConverter useCaseExtensionDtoToUseCaseExtensionConverter;

    public UseCaseMainStepDtoToUseCaseMainStepConverter(UseCaseExtensionDtoToUseCaseExtensionConverter useCaseExtensionDtoToUseCaseExtensionConverter) {
        this.useCaseExtensionDtoToUseCaseExtensionConverter = useCaseExtensionDtoToUseCaseExtensionConverter;
    }

    @Override
    public UseCaseMainStep convert(UseCaseMainStepDto source) {
        UseCaseMainStep useCaseMainStep = new UseCaseMainStep(
                source.id(),
                source.actor(),
                source.actionText()
        );

        useCaseMainStep.replaceExtensions(
                source.extensions().stream()
                        .map(this.useCaseExtensionDtoToUseCaseExtensionConverter::convert)
                        .toList()
        );

        return useCaseMainStep;
    }
}
