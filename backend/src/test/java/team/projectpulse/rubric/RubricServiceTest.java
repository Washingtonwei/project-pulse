package team.projectpulse.rubric;

import team.projectpulse.system.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RubricServiceTest {

    @Mock
    RubricRepository rubricRepository;

    @Mock
    CriterionRepository criterionRepository;

    @Mock
    UserUtils userUtils;

    @InjectMocks
    RubricService rubricService;

    Rubric rubric;

    Criterion criterion1;

    @BeforeEach
    void setUp() {
        // Create several peer evaluation criteria
        Criterion criterion1 = new Criterion("Quality of work", "How do you rate the quality of this teammate's work? (1-10)", 10.00);
        Criterion criterion2 = new Criterion("Productivity", "How productive is this teammate? (1-10)", 10.00);
        Criterion criterion3 = new Criterion("Initiative", "How proactive is this teammate? (1-10)", 10.00);
        Criterion criterion4 = new Criterion("Courtesy", "Does this teammate treat others with respect? (1-10)", 10.00);
        Criterion criterion5 = new Criterion("Open-mindedness", "How well does this teammate handle criticism of their work? (1-10)", 10.00);
        Criterion criterion6 = new Criterion("Engagement in meetings", "How is this teammate's performance during meetings? (1-10)", 10.00);
        Set<Criterion> criteria = Set.of(criterion1, criterion2, criterion3, criterion4, criterion5, criterion6);

        // Create a rubric with the criteria
        this.rubric = new Rubric("Peer Eval Rubric v1");
        criteria.forEach(this.rubric::addCriterion);
        this.criterion1 = criterion1;
    }

    @Test
    void testFindByCriteria() {
        // Given
        Map<String, String> searchCriteria = Map.of("rubricName", "rubric");
        PageImpl expectedPage = new PageImpl(List.of(this.rubric));

        given(this.userUtils.getUserCourseId()).willReturn(1);
        given(this.rubricRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Rubric> result = this.rubricService.findByCriteria(searchCriteria, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getRubricName()).isEqualTo("Peer Eval Rubric v1");
    }

    @Test
    void testFindRubricById() {
        // Given
        given(this.rubricRepository.findById(1)).willReturn(Optional.of(this.rubric));

        // When
        Rubric result = this.rubricService.findRubricById(1);

        // Then
        assertThat(result.getRubricName()).isEqualTo("Peer Eval Rubric v1");
    }

    @Test
    void testSaveRubric() {
        // Given
        Rubric newRubric = new Rubric("Peer Eval Rubric v2");
        given(this.rubricRepository.save(newRubric)).willReturn(newRubric);

        // When
        Rubric result = this.rubricService.saveRubric(newRubric);

        // Then
        assertThat(result.getRubricName()).isEqualTo("Peer Eval Rubric v2");
    }

    @Test
    void testUpdateRubric() {
        // Given
        Rubric update = new Rubric("Peer Eval Rubric v1 (updated)");

        given(this.rubricRepository.findById(1)).willReturn(Optional.of(this.rubric));
        given(this.rubricRepository.save(this.rubric)).willReturn(this.rubric);

        // When
        Rubric result = this.rubricService.updateRubric(1, update);

        // Then
        assertThat(result.getRubricName()).isEqualTo(update.getRubricName());
    }

    @Test
    void testDeleteRubric() {
        // Given
        given(this.rubricRepository.findById(1)).willReturn(Optional.of(this.rubric));
        doNothing().when(this.rubricRepository).deleteById(1);

        // When
        this.rubricService.deleteRubric(1);

        // Then
        verify(this.rubricRepository, times(1)).deleteById(1);
    }

    @Test
    void testAddCriterionToRubric() {
        // Given
        Criterion criterion7 = new Criterion("Team collaboration", "Assesses the effectiveness of team interactions, including communication, participation, contribution to discussions, willingness to help others, and overall teamwork dynamics. (1-10)", 10.00);
        criterion7.setCriterionId(7);

        given(this.rubricRepository.findById(1)).willReturn(Optional.of(this.rubric));
        given(this.criterionRepository.findById(7)).willReturn(Optional.of(criterion7));

        // When
        this.rubricService.addCriterionToRubric(1, 7);

        // Then
        assertThat(this.rubric.getCriteria().size()).isEqualTo(7);
    }

    @Test
    void testRemoveCriterionFromRubric() {
        // Given
        given(this.rubricRepository.findById(1)).willReturn(Optional.of(this.rubric));
        given(this.criterionRepository.findById(1)).willReturn(Optional.of(this.criterion1));

        // When
        this.rubricService.removeCriterionFromRubric(1, 1);

        // Then
        assertThat(this.rubric.getCriteria().size()).isEqualTo(5);
    }

}