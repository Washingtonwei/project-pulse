package team.projectpulse.ram.usecase.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import team.projectpulse.ram.usecase.UseCaseExtension;
import team.projectpulse.ram.usecase.dto.UseCaseExtensionDto;

@Component
public class UseCaseExtensionToUseCaseExtensionDtoConverter implements Converter<UseCaseExtension, UseCaseExtensionDto> {

    private final UseCaseExtensionStepToUseCaseExtensionStepDtoConverter useCaseExtensionStepToUseCaseExtensionStepDtoConverter;

    public UseCaseExtensionToUseCaseExtensionDtoConverter(UseCaseExtensionStepToUseCaseExtensionStepDtoConverter useCaseExtensionStepToUseCaseExtensionStepDtoConverter) {
        this.useCaseExtensionStepToUseCaseExtensionStepDtoConverter = useCaseExtensionStepToUseCaseExtensionStepDtoConverter;
    }

    @Override
    public UseCaseExtensionDto convert(UseCaseExtension source) {
        return new UseCaseExtensionDto(
                source.getId(),
                source.getConditionText(),
                source.getKind(),
                source.getExtensionExit(),
                source.getSteps().stream().map(this.useCaseExtensionStepToUseCaseExtensionStepDtoConverter::convert).toList()
        );
    }
}
