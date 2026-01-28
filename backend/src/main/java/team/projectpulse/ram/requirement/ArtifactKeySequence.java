package team.projectpulse.ram.requirement;

import jakarta.persistence.*;
import team.projectpulse.team.Team;

@Entity
public class ArtifactKeySequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementArtifactType type;

    @Column(nullable = false)
    private long nextNumber = 1;

    /**
     * Optional but recommended: optimistic versioning for safety if you ever switch lock strategy.
     */
    @Version
    private Long version;


    public ArtifactKeySequence() {
    }

    public ArtifactKeySequence(Team team, RequirementArtifactType type) {
        this.team = team;
        this.type = type;
        this.nextNumber = 1;
    }

    public long getNextNumber() {
        return nextNumber;
    }

    public void setNextNumber(long nextNumber) {
        this.nextNumber = nextNumber;
    }

    public Team getTeam() {
        return team;
    }

    public RequirementArtifactType getType() {
        return type;
    }

}