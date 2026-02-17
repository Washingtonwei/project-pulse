package team.projectpulse.ram.usecase;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.usecase.converter.UseCaseLockToUseCaseLockDtoConverter;
import team.projectpulse.ram.usecase.converter.UseCaseDtoToUseCaseConverter;
import team.projectpulse.ram.usecase.converter.UseCaseToUseCaseDtoConverter;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.ram.usecase.dto.UseCaseLockDto;
import team.projectpulse.ram.usecase.dto.UseCaseLockRequest;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class UseCaseController {

    private final UseCaseService useCaseService;
    private final UseCaseDtoToUseCaseConverter useCaseDtoToUseCaseConverter;
    private final UseCaseToUseCaseDtoConverter useCaseToUseCaseDtoConverter;
    private final UseCaseLockToUseCaseLockDtoConverter useCaseLockToUseCaseLockDtoConverter;

    public UseCaseController(UseCaseService useCaseService, UseCaseDtoToUseCaseConverter useCaseDtoToUseCaseConverter, UseCaseToUseCaseDtoConverter useCaseToUseCaseDtoConverter, UseCaseLockToUseCaseLockDtoConverter useCaseLockToUseCaseLockDtoConverter) {
        this.useCaseService = useCaseService;
        this.useCaseDtoToUseCaseConverter = useCaseDtoToUseCaseConverter;
        this.useCaseToUseCaseDtoConverter = useCaseToUseCaseDtoConverter;
        this.useCaseLockToUseCaseLockDtoConverter = useCaseLockToUseCaseLockDtoConverter;
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
        UseCase updateUseCase = this.useCaseService.updateUseCase(useCaseId, update, useCaseDto.version());
        UseCaseDto updatedUseCaseDto = this.useCaseToUseCaseDtoConverter.convert(updateUseCase);
        return new Result(true, StatusCode.SUCCESS, "Update use case successfully", updatedUseCaseDto);
    }

    @GetMapping("/teams/{teamId}/use-cases/{useCaseId}/lock")
    public Result getUseCaseLockStatus(@PathVariable Integer teamId, @PathVariable Long useCaseId) {
        UseCaseLock lock = this.useCaseService.findUseCaseLock(teamId, useCaseId);
        UseCaseLockDto lockDto = this.useCaseLockToUseCaseLockDtoConverter.convert(lock);
        return new Result(true, StatusCode.SUCCESS, "Find lock status successfully", lockDto);
    }

    @PutMapping("/teams/{teamId}/use-cases/{useCaseId}/lock")
    public Result lockUseCase(@PathVariable Integer teamId, @PathVariable Long useCaseId, @RequestBody UseCaseLockRequest lockRequest) {
        String reason = lockRequest != null ? lockRequest.reason() : null;
        UseCaseLock lock = this.useCaseService.lockUseCase(teamId, useCaseId, reason);
        UseCaseLockDto lockDto = this.useCaseLockToUseCaseLockDtoConverter.convert(lock);
        return new Result(true, StatusCode.SUCCESS, "Lock use case successfully", lockDto);
    }

    @DeleteMapping("/teams/{teamId}/use-cases/{useCaseId}/lock")
    public Result unlockUseCase(@PathVariable Integer teamId, @PathVariable Long useCaseId) {
        this.useCaseService.unlockUseCase(teamId, useCaseId);
        return new Result(true, StatusCode.SUCCESS, "Unlock use case successfully");
    }

}
