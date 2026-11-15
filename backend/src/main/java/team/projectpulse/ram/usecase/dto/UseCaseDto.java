package team.projectpulse.ram.usecase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record UseCaseDto(
        Long id,
        String artifactKey,
        @NotEmpty(message = "Use case title must not be empty.")
        String title,
        @NotEmpty(message = "Use case description must not be empty.")
        String description,
        @NotNull(message = "Team ID is must not be null.")
        Integer teamId,
        @NotNull(message = "Primary actor ID must not be null.")
        Long primaryActorId,
        @NotNull(message = "Secondary actor IDs must not be null; send an empty list for none.")
        Set<@NotNull Long> secondaryActorIds,
        @NotEmpty(message = "Use case trigger must not be empty.")
        String trigger,
        @NotNull(message = "Preconditions must not be null; send an empty list for none.")
        List<@Valid ConditionDto> preconditions,
        @NotNull(message = "Postconditions must not be null; send an empty list for none.")
        List<@Valid ConditionDto> postconditions,
        @NotNull(message = "Main steps must not be null; send an empty list for none.")
        List<@Valid UseCaseMainStepDto> mainSteps,
        Priority priority,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        PeerEvaluationUserDto createdBy,
        PeerEvaluationUserDto updatedBy,
        Integer version,
        UseCaseLockDto lock) {

    /**
     * Copies the collections so the record cannot be changed through them. A missing one stays null, so that
     * {@code @NotNull} reports it as a 400 rather than it being read as empty.
     */
    public UseCaseDto {
        secondaryActorIds = secondaryActorIds == null ? null : Set.copyOf(secondaryActorIds);
        preconditions = preconditions == null ? null : List.copyOf(preconditions);
        postconditions = postconditions == null ? null : List.copyOf(postconditions);
        mainSteps = mainSteps == null ? null : List.copyOf(mainSteps);
    }
}
