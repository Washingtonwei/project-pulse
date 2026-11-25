package team.projectpulse.ram.requirement;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.requirement.converter.RequirementArtifactDtoToRequirementArtifactConverter;
import team.projectpulse.ram.requirement.converter.RequirementArtifactToRequirementArtifactDtoConverter;
import team.projectpulse.ram.requirement.dto.ArtifactKeyHolderDto;
import team.projectpulse.ram.requirement.dto.ArtifactKeySuggestionDto;
import team.projectpulse.ram.requirement.dto.ArtifactKeySuggestionRequest;
import team.projectpulse.ram.requirement.dto.ArtifactReferrerDto;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class RequirementArtifactController {

    private final RequirementArtifactService requirementArtifactService;
    private final ArtifactKeySuggester artifactKeySuggester;
    private final RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactToRequirementArtifactDtoConverter;
    private final RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter;


    public RequirementArtifactController(RequirementArtifactService requirementArtifactService, ArtifactKeySuggester artifactKeySuggester, RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactToRequirementArtifactDtoConverter, RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter) {
        this.requirementArtifactService = requirementArtifactService;
        this.artifactKeySuggester = artifactKeySuggester;
        this.requirementArtifactToRequirementArtifactDtoConverter = requirementArtifactToRequirementArtifactDtoConverter;
        this.requirementArtifactDtoToRequirementArtifactConverter = requirementArtifactDtoToRequirementArtifactConverter;
    }

    @PostMapping("/teams/{teamId}/requirement-artifacts/search")
    public Result findRequirementArtifactsByCriteria(@PathVariable Integer teamId, @RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<RequirementArtifact> requirementArtifactPage = this.requirementArtifactService.findByCriteria(teamId, searchCriteria, pageable);
        Page<RequirementArtifactDto> requirementArtifactDtoPage = requirementArtifactPage.map(this.requirementArtifactToRequirementArtifactDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find requirement artifacts successfully.", requirementArtifactDtoPage);
    }

    /**
     * Which artifact of the team holds an artifact key: the editor's Availability check as the student picks a key
     * (UC-ART-create-artifact, "Artifact key rules"). For example,
     * {@code GET /teams/1/requirement-artifacts/key-holder?artifactKey=FR-user-authentication} answers with that seeded
     * functional requirement's id, key, title and type, and {@code ?artifactKey=FR-export-csv} with no data: team 1
     * holds no such key, and team 2's is never found. The answer is a convenience; the create or rename checks again.
     */
    @GetMapping("/teams/{teamId}/requirement-artifacts/key-holder")
    public Result findKeyHolder(@PathVariable Integer teamId, @RequestParam(required = false) String artifactKey) {
        ArtifactKeyHolderDto holder = this.requirementArtifactService.findKeyHolder(teamId, artifactKey).orElse(null);
        return new Result(true, StatusCode.SUCCESS, "Find artifact key holder successfully.", holder);
    }

    /**
     * Suggests an artifact key when the student presses Suggest (UC-ART-create-artifact, "Artifact key rules"). Always a
     * success when the request is well formed, because no suggestion is a normal state (FR-AI-degradation): for the
     * example in {@link ArtifactKeySuggestionRequest} the answer is {@code {"artifactKey": "FR-export-csv",
     * "aiAvailable": true}} when the LLM answers, and {@code {"artifactKey": null, "aiAvailable": false}} while it is
     * unavailable, as in dev and the tests, where no provider is set. The team is the route's, for the route rule
     * only: nothing about the team is sent to the LLM.
     */
    @PostMapping("/teams/{teamId}/requirement-artifacts/key-suggestion")
    public Result suggestKey(@PathVariable Integer teamId, @Valid @RequestBody ArtifactKeySuggestionRequest request) {
        ArtifactKeySuggestionDto suggestion = this.artifactKeySuggester.suggest(request.type(), request.title(), request.content());
        return new Result(true, StatusCode.SUCCESS, "Suggest artifact key successfully.", suggestion);
    }

    @GetMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}")
    public Result findRequirementArtifactById(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId) {
        RequirementArtifact requirementArtifact = this.requirementArtifactService.findRequirementArtifactById(teamId, requirementArtifactId);
        RequirementArtifactDto requirementArtifactDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(requirementArtifact);
        return new Result(true, StatusCode.SUCCESS, "Find requirement artifact successfully.", requirementArtifactDto);
    }

    /**
     * Creates a requirement artifact in the home its type gives it (UC-ART-create-artifact). The type alone names the
     * home, so the body names no document or section. Two examples, both for team 1 (the {@code teamId} argument comes
     * from the URL, never the body):
     * <pre>
     * A stakeholder, POST /teams/1/requirement-artifacts with
     *   {"type": "STAKEHOLDER", "artifactKey": "STK-course-instructor", "title": "Course instructor", "content": "Grades the teams' work."}
     * calls createRequirementArtifact(1, artifact of type STAKEHOLDER with that title and content) and lands at the end
     * of the Vision and Scope's STAKEHOLDER_PROFILES list with the key it was sent, STK-course-instructor.
     *
     * An assumption, with
     *   {"type": "ASSUMPTION", "artifactKey": "ASM-campus-sign-in", "title": "Campus sign-in", "content": "Every user has a university account."}
     * lands in the SRS's ASSUMPTIONS_DEPENDENCIES list with its key, ASM-campus-sign-in. A business-level one is a
     * BUSINESS_ASSUMPTION instead, which lands in the Vision and Scope's list of the same name, with a key such as BAS-campus-sign-in.
     * </pre>
     * A type with no list home (a glossary term, use case, precondition, postcondition or user story) is refused here,
     * because each has its own editor. The service would create it, since its own editor calls the same method.
     */
    @PostMapping("/teams/{teamId}/requirement-artifacts")
    public Result addRequirementArtifact(@PathVariable Integer teamId, @Valid @RequestBody RequirementArtifactDto requirementArtifactDto) {
        if (!requirementArtifactDto.type().hasListHome()) {
            throw RequirementArtifactService.notCreatedHere(requirementArtifactDto.type());
        }
        RequirementArtifact newRequirementArtifact = this.requirementArtifactDtoToRequirementArtifactConverter.convert(requirementArtifactDto);
        RequirementArtifact savedRequirementArtifact = this.requirementArtifactService.createRequirementArtifact(teamId, newRequirementArtifact);
        RequirementArtifactDto savedRequirementArtifactDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(savedRequirementArtifact);
        return new Result(true, StatusCode.SUCCESS, "Add requirement artifact successfully", savedRequirementArtifactDto);
    }

    @PutMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}")
    public Result updateRequirementArtifact(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId, @Valid @RequestBody RequirementArtifactDto requirementArtifactDto) {
        RequirementArtifact update = this.requirementArtifactDtoToRequirementArtifactConverter.convert(requirementArtifactDto);
        RequirementArtifact updatedRequirementArtifact = this.requirementArtifactService.updateRequirementArtifact(teamId, requirementArtifactId, update, requirementArtifactDto.version());
        RequirementArtifactDto updatedRequirementArtifactDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(updatedRequirementArtifact);
        return new Result(true, StatusCode.SUCCESS, "Update requirement artifact successfully", updatedRequirementArtifactDto);
    }

    /**
     * What refers to a requirement artifact, asked by the editor before it offers the delete (UC-ART-delete-artifact
     * step 4). For example, {@code GET /teams/1/requirement-artifacts/9/references} for FR-user-authentication returns its link to UC-create-use-case;
     * an empty list means the delete may go ahead. The DELETE checks again, so this answer is advisory.
     */
    @GetMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}/references")
    public Result findReferences(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId) {
        List<ArtifactReferrerDto> referrers = this.requirementArtifactService.findReferences(teamId, requirementArtifactId);
        return new Result(true, StatusCode.SUCCESS, "Find requirement artifact references successfully.", referrers);
    }

    /**
     * Deletes a requirement artifact for good (UC-ART-delete-artifact), for example
     * {@code DELETE /teams/1/requirement-artifacts/11?version=0} for FR-change-notification. The version is a query parameter, since a
     * body on DELETE is unreliable; it is optional here so that its absence gets the service's message rather than the
     * generic "malformed" one.
     */
    @DeleteMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}")
    public Result deleteRequirementArtifact(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId,
                                            @RequestParam(required = false) Integer version) {
        this.requirementArtifactService.deleteRequirementArtifact(teamId, requirementArtifactId, version);
        return new Result(true, StatusCode.SUCCESS, "Delete requirement artifact successfully", null);
    }

}
