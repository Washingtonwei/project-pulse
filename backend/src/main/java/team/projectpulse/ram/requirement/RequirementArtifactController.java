package team.projectpulse.ram.requirement;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.requirement.converter.RequirementArtifactDtoToRequirementArtifactConverter;
import team.projectpulse.ram.requirement.converter.RequirementArtifactToRequirementArtifactDtoConverter;
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
