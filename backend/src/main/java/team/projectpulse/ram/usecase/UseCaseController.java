package team.projectpulse.ram.usecase;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.ram.usecase.converter.UseCaseLockToUseCaseLockDtoConverter;
import team.projectpulse.ram.usecase.converter.UseCaseDtoToUseCaseConverter;
import team.projectpulse.ram.usecase.converter.UseCaseToUseCaseDtoConverter;
import team.projectpulse.ram.usecase.dto.ConditionDto;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.ram.usecase.dto.UseCaseLockDto;
import team.projectpulse.ram.usecase.dto.UseCaseLockRequest;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.List;

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
        UseCase useCase = this.useCaseService.findUseCaseByIdWithFullGraph(teamId, useCaseId);
        /**
         * At this point, the use case entity graph is fully loaded in the persistence context
         * The converter will NOT trigger any lazy loading, this is important for performance
         */
        UseCaseDto useCaseDto = this.useCaseToUseCaseDtoConverter.convert(useCase, this.useCaseService.otherUseCasesReferencing(useCase));
        return new Result(true, StatusCode.SUCCESS, "Find use case successfully", useCaseDto);
    }

    /**
     * Creates a use case (UC-DOC-create-use-case). The one request body, {@link UseCaseDto}, is read in two parts,
     * and every field ends up in exactly one of them:
     * <ul>
     *   <li>{@code useCaseDtoToUseCaseConverter} maps the plain fields into a new, unsaved {@link UseCase}: title,
     *       description, priority and notes (onto its artifact, whose type it sets to {@code USE_CASE}), the trigger,
     *       and the step tree (main steps, their extensions and extension steps). It resolves no id, so the
     *       {@code UseCase} it returns has no actors, no conditions and no team.</li>
     *   <li>{@link #referencesOf} copies the references out of the DTO unchanged: the actor ids, and each
     *       precondition and postcondition as its optional id plus its wording, in order.</li>
     * </ul>
     * The service then resolves those references through lookups scoped to {@code teamId} from the route, saves any
     * new condition, and gives the use case that team. The response lists, for each condition, the other use cases
     * that also reference it. The body's own {@code teamId} is never read. Fields the DTO carries only for
     * responses ({@code id}, {@code artifactKey}, {@code version}, the audit fields, {@code lock}) are ignored here.
     */
    @PostMapping("/teams/{teamId}/use-cases")
    public Result addUseCase(@PathVariable Integer teamId, @Valid @RequestBody UseCaseDto useCaseDto) {
        // Part 1: the plain fields and the step tree (main steps, their extensions and extension steps), as an unsaved use
        // case with no actors, no conditions and no team. saveUseCase replaces the steps with fresh copies without ids.
        UseCase newUseCase = this.useCaseDtoToUseCaseConverter.convert(useCaseDto);
        // Part 2: the references (actor ids, conditions), passed beside it and resolved by the service in the route's team.
        UseCase savedUseCase = this.useCaseService.saveUseCase(teamId, newUseCase, referencesOf(useCaseDto));
        UseCaseDto savedUseCaseDto = this.useCaseToUseCaseDtoConverter.convert(savedUseCase, this.useCaseService.otherUseCasesReferencing(savedUseCase));
        return new Result(true, StatusCode.SUCCESS, "Add use case successfully", savedUseCaseDto);
    }

    /**
     * Updates a use case (UC-DOC-edit-use-case). The body is read in the same two parts as in {@link #addUseCase},
     * plus a third value:
     * <ul>
     *   <li>the converter maps the plain fields into a detached {@link UseCase}, {@code update}, which is never saved
     *       itself: the service copies its fields onto the stored use case, and pairs its steps with the stored ones
     *       by id. The step, extension and extension-step ids are the only ids the converter keeps, because they are
     *       pairing keys rather than references;</li>
     *   <li>{@link #referencesOf} copies the references (actor ids, conditions) out of the DTO, for the service to
     *       resolve in the route's team;</li>
     *   <li>{@code useCaseDto.version()} is the version the editor last read, read straight from the DTO, because it
     *       is not a field of the use case being edited but the guard on the edit: a stale one is refused with 409.</li>
     * </ul>
     * Which use case is edited comes from {@code useCaseId} in the route, never from the body's {@code id}.
     */
    @PutMapping("/teams/{teamId}/use-cases/{useCaseId}")
    public Result updateUseCase(@PathVariable Integer teamId, @PathVariable Long useCaseId, @Valid @RequestBody UseCaseDto useCaseDto) {
        // Part 1: the plain fields and the step tree (main steps, their extensions and extension steps), as a detached
        // use case the service copies from; the step ids pair submitted steps with the stored ones.
        UseCase update = this.useCaseDtoToUseCaseConverter.convert(useCaseDto);
        // Parts 2 and 3: the references and the expected version, both read from the DTO and passed beside it.
        UseCase updateUseCase = this.useCaseService.updateUseCase(teamId, useCaseId, update, referencesOf(useCaseDto), useCaseDto.version());
        UseCaseDto updatedUseCaseDto = this.useCaseToUseCaseDtoConverter.convert(updateUseCase, this.useCaseService.otherUseCasesReferencing(updateUseCase));
        return new Result(true, StatusCode.SUCCESS, "Update use case successfully", updatedUseCaseDto);
    }

    /**
     * Copies the references out of the request body: the actor ids, the business rule ids, and the conditions in order. The converter
     * deliberately does not handle these: turning an id into an artifact needs the team from the route, which a
     * converter never sees, so the ids travel to the service as plain values and the service resolves them (OI-46,
     * BR-team-scoped-access). Nothing is looked up or checked here; a missing list, a missing primary actor or a
     * condition with empty wording was already refused by {@code @Valid} before this runs.
     */
    private static UseCaseReferences referencesOf(UseCaseDto useCaseDto) {
        return new UseCaseReferences(useCaseDto.primaryActorId(), useCaseDto.secondaryActorIds(), useCaseDto.businessRuleIds(),
                submittedConditions(useCaseDto.preconditions()), submittedConditions(useCaseDto.postconditions()));
    }

    /**
     * Keeps only what a request may say about a condition: which existing one it is, if any, and its wording. The
     * {@code type}, {@code priority} and {@code notes} of a request entry are ignored; the type comes from the list.
     */
    private static List<SubmittedCondition> submittedConditions(List<ConditionDto> conditions) {
        return conditions.stream().map(condition -> new SubmittedCondition(condition.id(), condition.condition())).toList();
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
