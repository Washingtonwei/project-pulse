package team.projectpulse.rubric;

import team.projectpulse.rubric.converter.RubricDtoToRubricConverter;
import team.projectpulse.rubric.converter.RubricToRubricDtoConverter;
import team.projectpulse.rubric.dto.RubricDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/rubrics")
public class RubricController {

    private final RubricService rubricService;
    private final RubricToRubricDtoConverter rubricToRubricDtoConverter;
    private final RubricDtoToRubricConverter rubricDtoToRubricConverter;


    public RubricController(RubricService rubricService, RubricToRubricDtoConverter rubricToRubricDtoConverter, RubricDtoToRubricConverter rubricDtoToRubricConverter) {
        this.rubricService = rubricService;
        this.rubricToRubricDtoConverter = rubricToRubricDtoConverter;
        this.rubricDtoToRubricConverter = rubricDtoToRubricConverter;
    }


    @PostMapping("/search")
    public Result findRubricsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Rubric> rubricPage = this.rubricService.findByCriteria(searchCriteria, pageable);
        Page<RubricDto> rubricDtoPage = rubricPage.map(this.rubricToRubricDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find rubrics successfully", rubricDtoPage);
    }

    @GetMapping("/{rubricId}")
    public Result findRubricById(@PathVariable Integer rubricId) {
        Rubric rubric = this.rubricService.findRubricById(rubricId);
        RubricDto rubricDto = this.rubricToRubricDtoConverter.convert(rubric);
        return new Result(true, StatusCode.SUCCESS, "Find rubric successfully", rubricDto);
    }

    @PostMapping
    public Result addRubric(@Valid @RequestBody RubricDto rubricDto) {
        Rubric newRubric = this.rubricDtoToRubricConverter.convert(rubricDto);
        Rubric savedRubric = this.rubricService.saveRubric(newRubric);
        RubricDto savedRubricDto = this.rubricToRubricDtoConverter.convert(savedRubric);
        return new Result(true, StatusCode.SUCCESS, "Add rubric successfully", savedRubricDto);
    }

    @PutMapping("/{rubricId}")
    public Result updateRubric(@PathVariable Integer rubricId, @Valid @RequestBody RubricDto rubricDto) {
        Rubric update = this.rubricDtoToRubricConverter.convert(rubricDto);
        Rubric updatedRubric = this.rubricService.updateRubric(rubricId, update);
        RubricDto updatedRubricDto = this.rubricToRubricDtoConverter.convert(updatedRubric);
        return new Result(true, StatusCode.SUCCESS, "Update rubric successfully", updatedRubricDto);
    }

    @DeleteMapping("/{rubricId}")
    public Result deleteRubric(@PathVariable Integer rubricId) {
        this.rubricService.deleteRubric(rubricId);
        return new Result(true, StatusCode.SUCCESS, "Delete rubric successfully", null);
    }

    @PutMapping("/{rubricId}/criteria/{criterionId}")
    public Result assignCriterionToRubric(@PathVariable Integer rubricId, @PathVariable Integer criterionId) {
        this.rubricService.addCriterionToRubric(rubricId, criterionId);
        return new Result(true, StatusCode.SUCCESS, "Add criterion to rubric successfully", null);
    }

    @DeleteMapping("/{rubricId}/criteria/{criterionId}")
    public Result removeCriterionFromRubric(@PathVariable Integer rubricId, @PathVariable Integer criterionId) {
        this.rubricService.removeCriterionFromRubric(rubricId, criterionId);
        return new Result(true, StatusCode.SUCCESS, "Remove criterion from rubric successfully", null);
    }

}
