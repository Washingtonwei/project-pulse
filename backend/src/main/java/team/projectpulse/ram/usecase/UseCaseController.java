package team.projectpulse.ram.usecase;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.usecase.converter.UseCaseDtoToUseCaseConverter;
import team.projectpulse.ram.usecase.converter.UseCaseToUseCaseDtoConverter;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class UseCaseController {

    private final UseCaseService useCaseService;
    private final UseCaseDtoToUseCaseConverter useCaseDtoToUseCaseConverter;
    private final UseCaseToUseCaseDtoConverter useCaseToUseCaseDtoConverter;

    public UseCaseController(UseCaseService useCaseService, UseCaseDtoToUseCaseConverter useCaseDtoToUseCaseConverter, UseCaseToUseCaseDtoConverter useCaseToUseCaseDtoConverter) {
        this.useCaseService = useCaseService;
        this.useCaseDtoToUseCaseConverter = useCaseDtoToUseCaseConverter;
        this.useCaseToUseCaseDtoConverter = useCaseToUseCaseDtoConverter;
    }

    @GetMapping("/teams/{teamId}/use-cases/{useCaseId}")
    public Result findUseCaseById(@PathVariable Integer teamId, @PathVariable Long useCaseId) {
        UseCase useCase = this.useCaseService.findUseCaseByIdWithFullGraph(useCaseId);
        /**
         * At this point, the use case entity graph is fully loaded in the persistence context
         * The converter will NOT trigger any lazy loading, this is important for performance
         */
        UseCaseDto useCaseDto = this.useCaseToUseCaseDtoConverter.convert(useCase);
        return new Result(true, StatusCode.SUCCESS, "Find use case successfully", useCaseDto);
    }

    @PostMapping("/teams/{teamId}/use-cases")
    public Result addUseCase(@PathVariable Integer teamId, @Valid @RequestBody UseCaseDto useCaseDto) {
        UseCase newUseCase = this.useCaseDtoToUseCaseConverter.convert(useCaseDto);
        UseCase savedUseCase = this.useCaseService.saveUseCase(teamId, newUseCase);
        UseCaseDto savedUseCaseDto = this.useCaseToUseCaseDtoConverter.convert(savedUseCase);
        return new Result(true, StatusCode.SUCCESS, "Add use case successfully", savedUseCaseDto);
    }

    @PutMapping("/teams/{teamId}/use-cases/{useCaseId}")
    public Result updateUseCase(@PathVariable Integer teamId, @PathVariable Long useCaseId, @Valid @RequestBody UseCaseDto useCaseDto) {
        UseCase update = this.useCaseDtoToUseCaseConverter.convert(useCaseDto);
        UseCase updateUseCase = this.useCaseService.updateUseCase(useCaseId, update);
        UseCaseDto updatedUseCaseDto = this.useCaseToUseCaseDtoConverter.convert(updateUseCase);
        return new Result(true, StatusCode.SUCCESS, "Update use case successfully", updatedUseCaseDto);
    }

}
