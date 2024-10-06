package team.projectpulse.evaluation;

import team.projectpulse.evaluation.converter.PeerEvaluationDtoToPeerEvaluationConverter;
import team.projectpulse.evaluation.converter.PeerEvaluationToPeerEvaluationDtoConverter;
import team.projectpulse.evaluation.dto.PeerEvaluationDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    private final PeerEvaluationToPeerEvaluationDtoConverter peerEvaluationToPeerEvaluationDtoConverter;

    private final PeerEvaluationDtoToPeerEvaluationConverter peerEvaluationDtoToPeerEvaluationConverter;


    public EvaluationController(EvaluationService evaluationService, PeerEvaluationToPeerEvaluationDtoConverter peerEvaluationToPeerEvaluationDtoConverter, PeerEvaluationDtoToPeerEvaluationConverter peerEvaluationDtoToPeerEvaluationConverter) {
        this.evaluationService = evaluationService;
        this.peerEvaluationToPeerEvaluationDtoConverter = peerEvaluationToPeerEvaluationDtoConverter;
        this.peerEvaluationDtoToPeerEvaluationConverter = peerEvaluationDtoToPeerEvaluationConverter;
    }

    @PostMapping
    public Result addPeerEvaluation(@Valid @RequestBody PeerEvaluationDto peerEvaluationDto) {
        PeerEvaluation newPeerEvaluation = this.peerEvaluationDtoToPeerEvaluationConverter.convert(peerEvaluationDto);
        PeerEvaluation savedPeerEvaluation = this.evaluationService.addPeerEvaluation(newPeerEvaluation);
        PeerEvaluationDto savedPeerEvaluationDto = this.peerEvaluationToPeerEvaluationDtoConverter.convert(savedPeerEvaluation);
        return new Result(true, StatusCode.SUCCESS, "Add peer evaluation successfully", savedPeerEvaluationDto);
    }

    @GetMapping("/evaluators/{evaluatorId}/week/{week}")
    public Result getPeerEvaluationsByEvaluatorIdAndWeek(@PathVariable Integer evaluatorId, @PathVariable String week) {
        List<PeerEvaluation> evaluations = this.evaluationService.getPeerEvaluationsByEvaluatorIdAndWeek(evaluatorId, week);
        List<PeerEvaluationDto> peerEvaluationDtos = evaluations.stream().map(this.peerEvaluationToPeerEvaluationDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Get peer evaluations by evaluator id and week successfully", peerEvaluationDtos);
    }

    @PutMapping("/{evaluationId}")
    public Result updatePeerEvaluation(@PathVariable Integer evaluationId, @Valid @RequestBody PeerEvaluationDto peerEvaluationDto) {
        PeerEvaluation update = this.peerEvaluationDtoToPeerEvaluationConverter.convert(peerEvaluationDto);
        PeerEvaluation updatedPeerEvaluation = this.evaluationService.updatePeerEvaluation(evaluationId, update);
        PeerEvaluationDto updatedPeerEvaluationDto = this.peerEvaluationToPeerEvaluationDtoConverter.convert(updatedPeerEvaluation);
        return new Result(true, StatusCode.SUCCESS, "Update peer evaluation successfully", updatedPeerEvaluationDto);
    }

    @GetMapping("/sections/{sectionId}/week/{week}")
    public Result generateWeeklyPeerEvaluationReportForSection(@PathVariable Integer sectionId, @PathVariable String week) {
        WeeklyPeerEvaluationReport report = this.evaluationService.generateWeeklyPeerEvaluationReportForSection(sectionId, week);
        return new Result(true, StatusCode.SUCCESS, "Generate weekly evaluations for section successfully", report);
    }

    @GetMapping("/students/{studentId}/week/{week}")
    public Result generateWeeklyPeerEvaluationSummaryForStudent(@PathVariable Integer studentId, @PathVariable String week) {
        PeerEvaluationAverage peerEvaluationAverage = this.evaluationService.generateWeeklyPeerEvaluationSummaryForStudent(studentId, week);
        return new Result(true, StatusCode.SUCCESS, "Generate weekly evaluation summary for student successfully", peerEvaluationAverage);
    }

    @GetMapping("/students/{studentId}/week/{week}/details")
    public Result getWeeklyPeerEvaluationsForStudent(@PathVariable Integer studentId, @PathVariable String week) {
        List<PeerEvaluation> evaluations = this.evaluationService.getWeeklyEvaluationsForStudent(studentId, week);
        List<PeerEvaluationDto> peerEvaluationDtos = evaluations.stream().map(this.peerEvaluationToPeerEvaluationDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Get weekly evaluations for student successfully", peerEvaluationDtos);
    }

    @GetMapping("/students/{studentId}")
    public Result generatePeerEvaluationSummariesForStudent(@PathVariable Integer studentId, @RequestParam String startWeek, @RequestParam String endWeek) {
        List<PeerEvaluationAverage> peerEvaluationAverages = this.evaluationService.generateEvaluationSummariesForStudent(studentId, startWeek, endWeek);
        return new Result(true, StatusCode.SUCCESS, "Generate evaluation summaries for student successfully", peerEvaluationAverages);
    }

}
