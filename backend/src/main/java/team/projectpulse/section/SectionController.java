package team.projectpulse.section;

import team.projectpulse.section.converter.SectionDtoToSectionConverter;
import team.projectpulse.section.converter.SectionToSectionDtoConverter;
import team.projectpulse.section.dto.SectionDto;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import team.projectpulse.user.userinvitation.UserInvitationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/sections")
public class SectionController {

    private final SectionService sectionService;
    private final SectionToSectionDtoConverter sectionToSectionDtoConverter;
    private final SectionDtoToSectionConverter sectionDtoToSectionConverter;
    private final UserInvitationService userInvitationService;


    public SectionController(SectionService sectionService, SectionToSectionDtoConverter sectionToSectionDtoConverter, SectionDtoToSectionConverter sectionDtoToSectionConverter, UserInvitationService userInvitationService) {
        this.sectionService = sectionService;
        this.sectionToSectionDtoConverter = sectionToSectionDtoConverter;
        this.sectionDtoToSectionConverter = sectionDtoToSectionConverter;
        this.userInvitationService = userInvitationService;
    }

    @PostMapping("/search")
    public Result findSectionsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Section> sectionPage = this.sectionService.findByCriteria(searchCriteria, pageable);
        Page<SectionDto> sectionDtoPage = sectionPage.map(this.sectionToSectionDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find sections successfully", sectionDtoPage);
    }

    @GetMapping("/{sectionId}")
    public Result findSectionById(@PathVariable Integer sectionId) {
        Section section = this.sectionService.findSectionById(sectionId);
        SectionDto sectionDto = this.sectionToSectionDtoConverter.convert(section);
        return new Result(true, StatusCode.SUCCESS, "Find section successfully", sectionDto);
    }

    @PostMapping
    public Result addSection(@Valid @RequestBody SectionDto sectionDto) {
        Section newSection = this.sectionDtoToSectionConverter.convert(sectionDto);
        Section savedSection = this.sectionService.saveSection(newSection);
        SectionDto savedSectionDto = this.sectionToSectionDtoConverter.convert(savedSection);
        return new Result(true, StatusCode.SUCCESS, "Add section successfully", savedSectionDto);
    }

    @PutMapping("/{sectionId}")
    public Result updateSection(@PathVariable Integer sectionId, @Valid @RequestBody SectionDto sectionDto) {
        Section update = this.sectionDtoToSectionConverter.convert(sectionDto);
        Section updatedSection = this.sectionService.updateSection(sectionId, update);
        SectionDto updatedSectionDto = this.sectionToSectionDtoConverter.convert(updatedSection);
        return new Result(true, StatusCode.SUCCESS, "Update section successfully", updatedSectionDto);
    }

    @DeleteMapping("/{sectionId}")
    public Result deleteSection(@PathVariable Integer sectionId) {
        this.sectionService.deleteSection(sectionId);
        return new Result(true, StatusCode.SUCCESS, "Delete section successfully", null);
    }

    @PostMapping("/{sectionId}/weeks")
    public Result setUpActiveWeeks(@PathVariable Integer sectionId, @RequestBody List<String> activeWeeks) {
        this.sectionService.setUpActiveWeeks(sectionId, activeWeeks);
        return new Result(true, StatusCode.SUCCESS, "Set active weeks successfully", null);
    }

    @PutMapping("/{sectionId}/rubrics/{rubricId}")
    public Result assignRubric(@PathVariable Integer sectionId, @PathVariable Integer rubricId) {
        this.sectionService.assignRubric(sectionId, rubricId);
        return new Result(true, StatusCode.SUCCESS, "Assign rubric successfully", null);
    }

    @PutMapping("/{sectionId}/instructors/{instructorId}")
    public Result assignInstructor(@PathVariable Integer sectionId, @PathVariable Integer instructorId) {
        this.sectionService.assignInstructor(sectionId, instructorId);
        return new Result(true, StatusCode.SUCCESS, "Assign instructor successfully", null);
    }

    @GetMapping("/{sectionId}/instructors")
    public Result getInstructors(@PathVariable Integer sectionId) {
        List<Map<String, Object>> instructors = this.sectionService.getInstructors(sectionId);
        return new Result(true, StatusCode.SUCCESS, "Get instructors successfully", instructors);
    }

    @DeleteMapping("/{sectionId}/instructors/{instructorId}")
    public Result removeInstructor(@PathVariable Integer sectionId, @PathVariable Integer instructorId) {
        this.sectionService.removeInstructor(sectionId, instructorId);
        return new Result(true, StatusCode.SUCCESS, "Remove instructor successfully", null);
    }

    @PostMapping("/{sectionId}/students/email-invitations")
    public Result sendEmailInvitationsToStudents(@RequestParam Integer courseId, @PathVariable Integer sectionId, @RequestBody List<String> emails) {
        this.userInvitationService.sendEmailInvitations(courseId, sectionId, emails, "student");
        return new Result(true, StatusCode.SUCCESS, "Send email invitation successfully", null);
    }

    @PostMapping("/{sectionId}/instructors/invite-or-add")
    public Result inviteOrAddInstructors(@RequestParam Integer courseId, @PathVariable Integer sectionId, @RequestBody List<String> emails) {
        Map<String, Object> result = this.sectionService.inviteOrAddInstructors(courseId, sectionId, emails);
        return new Result(true, StatusCode.SUCCESS, "Instructors invited or added successfully", result);
    }

}
