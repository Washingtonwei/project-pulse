package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseMainStep;
import team.projectpulse.ram.usecase.dto.UseCaseMainStepDto;

@Component
public class UseCaseMainStepToUseCaseMainStepDtoConverter implements Converter<UseCaseMainStep, UseCaseMainStepDto> {

    private final UseCaseExtensionToUseCaseExtensionDtoConverter useCaseExtensionToUseCaseExtensionDtoConverter;

    public UseCaseMainStepToUseCaseMainStepDtoConverter(UseCaseExtensionToUseCaseExtensionDtoConverter useCaseExtensionToUseCaseExtensionDtoConverter) {
        this.useCaseExtensionToUseCaseExtensionDtoConverter = useCaseExtensionToUseCaseExtensionDtoConverter;
    }

    @Override
    public UseCaseMainStepDto convert(UseCaseMainStep source) {
        return new UseCaseMainStepDto(
                source.getId(),
                source.getActor(),
                source.getActionText(),
                source.getExtensions().stream()
                        .map(this.useCaseExtensionToUseCaseExtensionDtoConverter::convert)
                        .toList()
        );
    }
}
