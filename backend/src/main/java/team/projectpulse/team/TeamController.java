package team.projectpulse.team;

import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import team.projectpulse.team.converter.TeamDtoToTeamConverter;
import team.projectpulse.team.converter.TeamToTeamDtoConverter;
import team.projectpulse.team.dto.TeamDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.team.dto.TransferTeamRequest;
import team.projectpulse.team.dto.TransferTeamResponse;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/teams")
public class TeamController {

    private final TeamService teamService;
    private final TeamToTeamDtoConverter teamToTeamDtoConverter;
    private final TeamDtoToTeamConverter teamDtoToTeamConverter;


    public TeamController(TeamService teamService, TeamToTeamDtoConverter teamToTeamDtoConverter, TeamDtoToTeamConverter teamDtoToTeamConverter) {
        this.teamService = teamService;
        this.teamToTeamDtoConverter = teamToTeamDtoConverter;
        this.teamDtoToTeamConverter = teamDtoToTeamConverter;
    }

    @PostMapping("/search")
    public Result findTeamsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Team> teamPage = this.teamService.findByCriteria(searchCriteria, pageable);
        Page<TeamDto> teamDtoPage = teamPage.map(this.teamToTeamDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find teams successfully", teamDtoPage);
    }

    @GetMapping("/{teamId}")
    public Result findTeamById(@PathVariable Integer teamId) {
        Team team = this.teamService.findTeamById(teamId);
        TeamDto teamDto = this.teamToTeamDtoConverter.convert(team);
        return new Result(true, StatusCode.SUCCESS, "Find team successfully", teamDto);
    }

    @PostMapping()
    public Result addTeam(@Valid @RequestBody TeamDto teamDto) {
        Team newTeam = this.teamDtoToTeamConverter.convert(teamDto);
        Team savedTeam = this.teamService.saveTeam(newTeam);
        TeamDto savedTeamDto = this.teamToTeamDtoConverter.convert(savedTeam);
        return new Result(true, StatusCode.SUCCESS, "Add team successfully", savedTeamDto);
    }

    @PutMapping("/{teamId}")
    public Result updateTeam(@PathVariable Integer teamId, @Valid @RequestBody TeamDto teamDto) {
        Team update = this.teamDtoToTeamConverter.convert(teamDto);
        Team updatedTeam = this.teamService.updateTeam(teamId, update);
        TeamDto updatedTeamDto = this.teamToTeamDtoConverter.convert(updatedTeam);
        return new Result(true, StatusCode.SUCCESS, "Update team successfully", updatedTeamDto);
    }

    @PatchMapping("/{teamId}/section")
    public Result transferTeamToAnotherSection(@PathVariable Integer teamId, @Valid @RequestBody TransferTeamRequest request) {
        TransferTeamResponse response = this.teamService.transferTeamToAnotherSection(teamId, request.sectionId());
        return new Result(true, StatusCode.SUCCESS, "Transfer team successfully", response);
    }

    @DeleteMapping("/{teamId}")
    public Result deleteTeam(@PathVariable Integer teamId) {
        this.teamService.deleteTeam(teamId);
        return new Result(true, StatusCode.SUCCESS, "Delete team successfully", null);
    }

    @PutMapping("/{teamId}/students/{studentId}")
    public Result assignStudentToTeam(@PathVariable Integer teamId, @PathVariable Integer studentId) {
        this.teamService.assignStudentToTeam(teamId, studentId);
        return new Result(true, StatusCode.SUCCESS, "Assign student to team successfully", null);
    }

    @DeleteMapping("/{teamId}/students/{studentId}")
    public Result removeStudentFromTeam(@PathVariable Integer teamId, @PathVariable Integer studentId) {
        this.teamService.removeStudentFromTeam(teamId, studentId);
        return new Result(true, StatusCode.SUCCESS, "Remove student from team successfully", null);
    }

    @PutMapping("/{teamId}/instructors/{instructorId}")
    public Result assignInstructorToTeam(@PathVariable Integer teamId, @PathVariable Integer instructorId) {
        this.teamService.assignInstructorToTeam(teamId, instructorId);
        return new Result(true, StatusCode.SUCCESS, "Assign instructor to team successfully", null);
    }

}
