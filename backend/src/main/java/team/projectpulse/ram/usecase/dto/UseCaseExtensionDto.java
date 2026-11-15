package team.projectpulse.ram.usecase.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.usecase.ExtensionExit;
import team.projectpulse.ram.usecase.ExtensionKind;

import java.util.List;

public record UseCaseExtensionDto(
        Long id,
        String conditionText,
        ExtensionKind kind,
        ExtensionExit exit,
        @NotNull(message = "Extension steps must not be null; send an empty list for none.")
        List<UseCaseExtensionStepDto> steps) {

    /**
     * Copies the list so the record cannot be changed through it. A missing one stays null, so that
     * {@code @NotNull} reports it as a 400 rather than it being read as empty.
     */
    public UseCaseExtensionDto {
        steps = steps == null ? null : List.copyOf(steps);
    }
}
