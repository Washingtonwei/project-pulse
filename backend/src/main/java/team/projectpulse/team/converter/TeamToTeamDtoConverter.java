package team.projectpulse.team.converter;

import team.projectpulse.team.Team;
import team.projectpulse.team.dto.TeamDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TeamToTeamDtoConverter implements Converter<Team, TeamDto> {

    @Override
    public TeamDto convert(Team team) {
        return new TeamDto(
                team.getTeamId(),
                team.getTeamName(),
                team.getDescription(),
                team.getTeamWebsiteUrl(),
                team.getSection().getSectionId(),
                team.getSection().getSectionName()
        );
    }

}
