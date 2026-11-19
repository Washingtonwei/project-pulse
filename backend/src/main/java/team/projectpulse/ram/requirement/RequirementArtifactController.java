package team.projectpulse.ram.requirement;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.requirement.converter.RequirementArtifactDtoToRequirementArtifactConverter;
import team.projectpulse.ram.requirement.converter.RequirementArtifactToRequirementArtifactDtoConverter;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class RequirementArtifactController {

    private final RequirementArtifactService requirementArtifactService;
    private final RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactToRequirementArtifactDtoConverter;
    private final RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter;


    public RequirementArtifactController(RequirementArtifactService requirementArtifactService, RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactToRequirementArtifactDtoConverter, RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter) {
        this.requirementArtifactService = requirementArtifactService;
        this.requirementArtifactToRequirementArtifactDtoConverter = requirementArtifactToRequirementArtifactDtoConverter;
        this.requirementArtifactDtoToRequirementArtifactConverter = requirementArtifactDtoToRequirementArtifactConverter;
    }

    @PostMapping("/teams/{teamId}/requirement-artifacts/search")
    public Result findRequirementArtifactsByCriteria(@PathVariable Integer teamId, @RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<RequirementArtifact> requirementArtifactPage = this.requirementArtifactService.findByCriteria(teamId, searchCriteria, pageable);
        Page<RequirementArtifactDto> requirementArtifactDtoPage = requirementArtifactPage.map(this.requirementArtifactToRequirementArtifactDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find requirement artifacts successfully.", requirementArtifactDtoPage);
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
     *   {"type": "STAKEHOLDER", "title": "Course instructor", "content": "Grades the teams' work."}
     * calls createRequirementArtifact(1, artifact of type STAKEHOLDER with that title and content) and lands at the end
     * of the Vision and Scope's STAKEHOLDER_PROFILES list with the next key, such as STK-3.
     *
     * An assumption, with
     *   {"type": "ASSUMPTION", "title": "Campus sign-in", "content": "Every user has a university account."}
     * lands in the SRS's ASSUMPTIONS_DEPENDENCIES list with a key such as ASM-2. A business-level one is a
     * BUSINESS_ASSUMPTION instead, which lands in the Vision and Scope's list of the same name (BAS-n).
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
     * Deletes a requirement artifact for good (UC-ART-delete-artifact), for example
     * {@code DELETE /teams/1/requirement-artifacts/11?version=0} for FR-3. The version is a query parameter, since a
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
