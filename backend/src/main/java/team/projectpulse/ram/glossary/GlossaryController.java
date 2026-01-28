package team.projectpulse.ram.glossary;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.converter.RequirementArtifactDtoToRequirementArtifactConverter;
import team.projectpulse.ram.requirement.converter.RequirementArtifactToRequirementArtifactDtoConverter;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class GlossaryController {

    private final GlossaryService glossaryService;
    private final RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactToRequirementArtifactDtoConverter;
    private final RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter;

    public GlossaryController(GlossaryService glossaryService, RequirementArtifactToRequirementArtifactDtoConverter requirementArtifactToRequirementArtifactDtoConverter, RequirementArtifactDtoToRequirementArtifactConverter requirementArtifactDtoToRequirementArtifactConverter) {
        this.glossaryService = glossaryService;
        this.requirementArtifactToRequirementArtifactDtoConverter = requirementArtifactToRequirementArtifactDtoConverter;
        this.requirementArtifactDtoToRequirementArtifactConverter = requirementArtifactDtoToRequirementArtifactConverter;
    }

    @GetMapping("/teams/{teamId}/glossary-terms/{glossaryTermId}")
    public Result findGlossaryTermById(@PathVariable Integer teamId, @PathVariable Long glossaryTermId) {
        RequirementArtifact glossaryTerm = this.glossaryService.findGlossaryTermById(teamId, glossaryTermId);
        RequirementArtifactDto glossaryTermDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(glossaryTerm);
        return new Result(true, StatusCode.SUCCESS, "Find glossary term successfully.", glossaryTermDto);
    }

    @PostMapping("/teams/{teamId}/glossary-terms")
    public Result createGlossaryTerm(@PathVariable Integer teamId, @RequestBody RequirementArtifactDto glossaryTermDto) {
        RequirementArtifact newGlossaryTerm = this.requirementArtifactDtoToRequirementArtifactConverter.convert(glossaryTermDto);
        RequirementArtifact saveGlossaryTerm = this.glossaryService.saveGlossaryTerm(teamId, newGlossaryTerm);
        RequirementArtifactDto savedGlossaryTermDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(saveGlossaryTerm);
        return new Result(true, StatusCode.SUCCESS, "Add glossary term successfully", savedGlossaryTermDto);
    }

    @PatchMapping("/teams/{teamId}/glossary-terms/{glossaryTermId}")
    public Result updateGlossaryTermDefinition(@PathVariable Integer teamId, @PathVariable Long glossaryTermId, @RequestBody RequirementArtifactDto glossaryTermDto) {
        RequirementArtifact update = this.requirementArtifactDtoToRequirementArtifactConverter.convert(glossaryTermDto);
        RequirementArtifact updatedGlossaryTerm = this.glossaryService.updateGlossaryTermDefinition(teamId, glossaryTermId, update);
        RequirementArtifactDto updatedGlossaryTermDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(updatedGlossaryTerm);
        return new Result(true, StatusCode.SUCCESS, "Update glossary term definition successfully", updatedGlossaryTermDto);
    }

    // Rename glossary term endpoint
    @PatchMapping("/teams/{teamId}/glossary-terms/{glossaryTermId}/rename")
    public Result renameGlossaryTerm(@PathVariable Integer teamId, @PathVariable Long glossaryTermId, @RequestBody RequirementArtifactDto glossaryTermDto) {
        RequirementArtifact update = this.requirementArtifactDtoToRequirementArtifactConverter.convert(glossaryTermDto);
        RequirementArtifact updatedGlossaryTerm = this.glossaryService.renameGlossaryTerm(glossaryTermId, update);
        RequirementArtifactDto updatedGlossaryTermDto = this.requirementArtifactToRequirementArtifactDtoConverter.convert(updatedGlossaryTerm);
        return new Result(true, StatusCode.SUCCESS, "Rename glossary term successfully", updatedGlossaryTermDto);
    }

}
