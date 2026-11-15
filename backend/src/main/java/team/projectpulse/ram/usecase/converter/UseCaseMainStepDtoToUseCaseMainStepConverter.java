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
        // The id is mapped on purpose, as a pairing key only. The result is never saved as it is: on create,
        // UseCaseService.saveUseCase replaces every step with a copyOf without an id; on edit, UseCase.reviseMainSteps
        // pairs it with one of the use case's own steps or builds such a fresh copy. So a body id can only select
        // among that use case's own rows (OI-46). The same holds for extensions and extension steps.
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
