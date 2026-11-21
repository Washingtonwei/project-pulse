package team.projectpulse.ram.usecase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
        Integer teamId, // optional and ignored: the team is always the route's
        @NotNull(message = "Primary actor ID must not be null.")
        Long primaryActorId,
        @NotNull(message = "Secondary actor IDs must not be null; send an empty list for none.")
        Set<@NotNull Long> secondaryActorIds,
        @NotNull(message = "Business rule IDs must not be null; send an empty list for none.")
        Set<@NotNull Long> businessRuleIds,
        @NotEmpty(message = "Use case trigger must not be empty.")
        String trigger,
        // Optional, plain text, one line (UC-DOC-create-use-case, Associated Information).
        @Size(max = 255, message = "Frequency of use must be at most 255 characters.")
        String frequencyOfUse,
        // Optional rich text (Tiptap HTML), stored as sent. A field left out arrives as null and clears the stored value.
        String associatedInformation,
        String assumptions,
        String openIssues,
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
        businessRuleIds = businessRuleIds == null ? null : Set.copyOf(businessRuleIds);
        preconditions = preconditions == null ? null : List.copyOf(preconditions);
        postconditions = postconditions == null ? null : List.copyOf(postconditions);
        mainSteps = mainSteps == null ? null : List.copyOf(mainSteps);
    }
}
