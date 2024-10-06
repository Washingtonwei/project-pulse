package team.projectpulse.team.converter;

import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.system.UserUtils;
import team.projectpulse.system.exception.ObjectNotFoundException;
import team.projectpulse.team.Team;
import team.projectpulse.team.dto.TeamDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TeamDtoToTeamConverter implements Converter<TeamDto, Team> {

    private final UserUtils userUtils;
    private final SectionRepository sectionRepository;


    public TeamDtoToTeamConverter(UserUtils userUtils, SectionRepository sectionRepository) {
        this.userUtils = userUtils;
        this.sectionRepository = sectionRepository;
    }

    @Override
    public Team convert(TeamDto teamDto) {
        Team team = new Team(teamDto.teamName(), teamDto.description(), teamDto.teamWebsiteUrl()); // Unless creating a new team, don't relate this team to a persisted section
        team.setTeamId(teamDto.teamId());
        if (teamDto.teamId() == null) { // When creating a new team, find and set the section; when updating a team, don't set the section
            Integer sectionId = this.userUtils.getUserSectionId();
            Section section = this.sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new ObjectNotFoundException("section", sectionId));
            section.addTeam(team);
        }
        return team;
    }

}
