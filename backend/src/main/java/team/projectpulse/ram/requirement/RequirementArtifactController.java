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

    @PostMapping("/teams/{teamId}/requirement-artifacts")
    public Result addRequirementArtifact(@PathVariable Integer teamId, @Valid @RequestBody RequirementArtifactDto requirementArtifactDto) {
        RequirementArtifact newRequirementArtifact = this.requirementArtifactDtoToRequirementArtifactConverter.convert(requirementArtifactDto);
        RequirementArtifact savedRequirementArtifact = this.requirementArtifactService.saveRequirementArtifact(teamId, newRequirementArtifact);
        RequirementArtifactDto savedRequirementArtifactDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(savedRequirementArtifact);
        return new Result(true, StatusCode.SUCCESS, "Add requirement artifact successfully", savedRequirementArtifactDto);
    }

    @PutMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}")
    public Result updateRequirementArtifact(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId, @Valid @RequestBody RequirementArtifactDto requirementArtifactDto) {
        RequirementArtifact update = this.requirementArtifactDtoToRequirementArtifactConverter.convert(requirementArtifactDto);
        RequirementArtifact updatedRequirementArtifact = this.requirementArtifactService.updateRequirementArtifact(teamId, requirementArtifactId, update);
        RequirementArtifactDto updatedRequirementArtifactDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(updatedRequirementArtifact);
        return new Result(true, StatusCode.SUCCESS, "Update requirement artifact successfully", updatedRequirementArtifactDto);
    }

    @DeleteMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}")
    public Result deleteRequirementArtifact(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId) {
        this.requirementArtifactService.deleteRequirementArtifact(teamId, requirementArtifactId);
        return new Result(true, StatusCode.SUCCESS, "Delete requirement artifact successfully", null);
    }

}
