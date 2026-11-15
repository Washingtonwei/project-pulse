package team.projectpulse.ram.usecase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UseCaseMainStepDto(
        Long id,
        String actor,
        String actionText,
        @NotNull(message = "Extensions must not be null; send an empty list for none.")
        List<@Valid UseCaseExtensionDto> extensions) {

    /**
     * Copies the list so the record cannot be changed through it. A missing one stays null, so that
     * {@code @NotNull} reports it as a 400 rather than it being read as empty.
     */
    public UseCaseMainStepDto {
        extensions = extensions == null ? null : List.copyOf(extensions);
    }
}
