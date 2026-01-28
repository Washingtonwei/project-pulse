package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseExtension;
import team.projectpulse.ram.usecase.dto.UseCaseExtensionDto;

@Component
public class UseCaseExtensionDtoToUseCaseExtensionConverter implements Converter<UseCaseExtensionDto, UseCaseExtension> {

    private final UseCaseExtensionStepDtoToUseCaseExtensionStepConverter useCaseExtensionStepDtoToUseCaseExtensionStepConverter;

    public UseCaseExtensionDtoToUseCaseExtensionConverter(UseCaseExtensionStepDtoToUseCaseExtensionStepConverter useCaseExtensionStepDtoToUseCaseExtensionStepConverter) {
        this.useCaseExtensionStepDtoToUseCaseExtensionStepConverter = useCaseExtensionStepDtoToUseCaseExtensionStepConverter;
    }

    @Override
    public UseCaseExtension convert(UseCaseExtensionDto source) {
        UseCaseExtension extension = new UseCaseExtension(
                source.id(),
                source.conditionText(),
                source.kind(),
                source.exit()
        );

        extension.replaceSteps(
                source.steps().stream()
                        .map(this.useCaseExtensionStepDtoToUseCaseExtensionStepConverter::convert)
                        .toList()
        );

        return extension;
    }
}
