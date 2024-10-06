package team.projectpulse.rubric;

import team.projectpulse.rubric.converter.CriterionDtoToCriterionConverter;
import team.projectpulse.rubric.converter.CriterionToCriterionDtoConverter;
import team.projectpulse.rubric.dto.CriterionDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/criteria")
public class CriterionController {

    private final CriterionService criterionService;
    private final CriterionToCriterionDtoConverter criterionToCriterionDtoConverter;
    private final CriterionDtoToCriterionConverter criterionDtoToCriterionConverter;


    public CriterionController(CriterionService criterionService, CriterionToCriterionDtoConverter criterionToCriterionDtoConverter, CriterionDtoToCriterionConverter criterionDtoToCriterionConverter) {
        this.criterionService = criterionService;
        this.criterionToCriterionDtoConverter = criterionToCriterionDtoConverter;
        this.criterionDtoToCriterionConverter = criterionDtoToCriterionConverter;
    }


    @PostMapping("/search")
    public Result findPeerEvaluationCriteriaByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Criterion> criterionPage = this.criterionService.findByCriteria(searchCriteria, pageable);
        Page<CriterionDto> criterionDtoPage = criterionPage.map(this.criterionToCriterionDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find peer evaluation criteria successfully", criterionDtoPage);
    }

    @GetMapping("/{criterionId}")
    public Result findPeerEvaluationCriterionById(@PathVariable Integer criterionId) {
        Criterion criterion = this.criterionService.findCriterionById(criterionId);
        CriterionDto criterionDto = this.criterionToCriterionDtoConverter.convert(criterion);
        return new Result(true, StatusCode.SUCCESS, "Find peer evaluation criterion successfully", criterionDto);
    }

    @PostMapping
    public Result addCriterion(@Valid @RequestBody CriterionDto criterionDto) {
        Criterion newCriterion = this.criterionDtoToCriterionConverter.convert(criterionDto);
        Criterion savedCriterion = this.criterionService.saveCriterion(newCriterion);
        CriterionDto savedCriterionDto = this.criterionToCriterionDtoConverter.convert(savedCriterion);
        return new Result(true, StatusCode.SUCCESS, "Add peer evaluation criterion successfully", savedCriterionDto);
    }

    @PutMapping("/{criterionId}")
    public Result updateCriterion(@PathVariable Integer criterionId, @Valid @RequestBody CriterionDto criterionDto) {
        Criterion update = this.criterionDtoToCriterionConverter.convert(criterionDto);
        Criterion updatedCriterion = this.criterionService.updateCriterion(criterionId, update);
        CriterionDto updatedCriterionDto = this.criterionToCriterionDtoConverter.convert(updatedCriterion);
        return new Result(true, StatusCode.SUCCESS, "Update peer evaluation criterion successfully", updatedCriterionDto);
    }

    @DeleteMapping("/{criterionId}")
    public Result deleteCriterion(@PathVariable Integer criterionId) {
        this.criterionService.deleteCriterion(criterionId);
        return new Result(true, StatusCode.SUCCESS, "Delete peer evaluation criterion successfully", null);
    }

}
