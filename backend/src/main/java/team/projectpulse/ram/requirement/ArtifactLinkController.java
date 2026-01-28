package team.projectpulse.ram.requirement;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.requirement.converter.ArtifactLinkToArtifactLinkDtoConverter;
import team.projectpulse.ram.requirement.converter.ArtifactLinkToDownstreamLinkViewDtoConverter;
import team.projectpulse.ram.requirement.converter.ArtifactLinkToUpstreamLinkViewDtoConverter;
import team.projectpulse.ram.requirement.dto.ArtifactLinkDto;
import team.projectpulse.ram.requirement.dto.ArtifactTraceability;
import team.projectpulse.ram.requirement.dto.CreateArtifactLinkRequest;
import team.projectpulse.ram.requirement.dto.UpdateArtifactLinkRequest;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class ArtifactLinkController {

    private final ArtifactLinkService artifactLinkService;
    private final ArtifactLinkToArtifactLinkDtoConverter artifactLinkToArtifactLinkDtoConverter;
    private final ArtifactLinkToUpstreamLinkViewDtoConverter artifactLinkToUpstreamLinkViewDtoConverter;
    private final ArtifactLinkToDownstreamLinkViewDtoConverter artifactLinkToDownstreamLinkViewDtoConverter;

    public ArtifactLinkController(ArtifactLinkService artifactLinkService, ArtifactLinkToArtifactLinkDtoConverter artifactLinkToArtifactLinkDtoConverter, ArtifactLinkToUpstreamLinkViewDtoConverter artifactLinkToUpstreamLinkViewDtoConverter, ArtifactLinkToDownstreamLinkViewDtoConverter artifactLinkToDownstreamLinkViewDtoConverter) {
        this.artifactLinkService = artifactLinkService;
        this.artifactLinkToArtifactLinkDtoConverter = artifactLinkToArtifactLinkDtoConverter;
        this.artifactLinkToUpstreamLinkViewDtoConverter = artifactLinkToUpstreamLinkViewDtoConverter;
        this.artifactLinkToDownstreamLinkViewDtoConverter = artifactLinkToDownstreamLinkViewDtoConverter;
    }

    @PostMapping("/teams/{teamId}/artifact-links/search")
    public Result findArtifactLinksByCriteria(@PathVariable Integer teamId, @RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<ArtifactLink> artifactLinksPage = this.artifactLinkService.findByCriteria(teamId, searchCriteria, pageable);
        Page<ArtifactLinkDto> artifactLinkDtosPage = artifactLinksPage.map(this.artifactLinkToArtifactLinkDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find artifact links successfully", artifactLinkDtosPage);
    }

    @GetMapping("/teams/{teamId}/artifact-links/{artifactLinkId}")
    public Result getArtifactLinkById(@PathVariable Integer teamId, @PathVariable Long artifactLinkId) {
        ArtifactLink artifactLink = this.artifactLinkService.getArtifactLinkById(teamId, artifactLinkId);
        ArtifactLinkDto artifactLinkDto = this.artifactLinkToArtifactLinkDtoConverter.convert(artifactLink);
        return new Result(true, StatusCode.SUCCESS, "Get artifact link successfully", artifactLinkDto);
    }

    @PostMapping("/teams/{teamId}/artifact-links")
    public Result addArtifactLink(@PathVariable Integer teamId, @Valid @RequestBody CreateArtifactLinkRequest createArtifactLinkRequest) {
        ArtifactLink artifactLink = this.artifactLinkService.createArtifactLink(teamId, createArtifactLinkRequest);
        ArtifactLinkDto artifactLinkDto = this.artifactLinkToArtifactLinkDtoConverter.convert(artifactLink);
        return new Result(true, StatusCode.SUCCESS, "Add artifact link successfully", artifactLinkDto);
    }

    @PutMapping("/teams/{teamId}/artifact-links/{artifactLinkId}")
    public Result updateArtifactLink(@PathVariable Integer teamId, @PathVariable Long artifactLinkId, @Valid @RequestBody UpdateArtifactLinkRequest updateArtifactLinkRequest) {
        ArtifactLink artifactLink = this.artifactLinkService.updateArtifactLink(teamId, artifactLinkId, updateArtifactLinkRequest);
        ArtifactLinkDto artifactLinkDto = this.artifactLinkToArtifactLinkDtoConverter.convert(artifactLink);
        return new Result(true, StatusCode.SUCCESS, "Update artifact link successfully", artifactLinkDto);
    }

    @DeleteMapping("/teams/{teamId}/artifact-links/{artifactLinkId}")
    public Result deleteArtifactLink(@PathVariable Integer teamId, @PathVariable Long artifactLinkId) {
        this.artifactLinkService.deleteArtifactLink(teamId, artifactLinkId);
        return new Result(true, StatusCode.SUCCESS, "Delete artifact link successfully");
    }

    @GetMapping("/teams/{teamId}/requirement-artifacts/{requirementArtifactId}/traceability")
    public Result getArtifactTraceabilityByRequirementArtifactId(@PathVariable Integer teamId, @PathVariable Long requirementArtifactId) {
        List<ArtifactLink> outgoingLinks = this.artifactLinkService.getOutgoingLinksByRequirementArtifactId(teamId, requirementArtifactId);
        List<ArtifactLink> incomingLinks = this.artifactLinkService.getIncomingLinksByRequirementArtifactId(teamId, requirementArtifactId);
        ArtifactTraceability artifactTraceability = new ArtifactTraceability(requirementArtifactId,
                incomingLinks.stream().map(this.artifactLinkToUpstreamLinkViewDtoConverter::convert).toList(),
                outgoingLinks.stream().map(this.artifactLinkToDownstreamLinkViewDtoConverter::convert).toList());
        return new Result(true, StatusCode.SUCCESS, "Get artifact links successfully", artifactTraceability);
    }

}
