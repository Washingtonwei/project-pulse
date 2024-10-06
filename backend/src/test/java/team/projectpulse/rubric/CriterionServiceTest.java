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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriterionServiceTest {

    @Mock
    CriterionRepository criterionRepository;

    @Mock
    UserUtils userUtils;

    @InjectMocks
    CriterionService criterionService;

    List<Criterion> criteria;

    @BeforeEach
    void setUp() {
        Criterion criterion1 = new Criterion("Quality of work", "How do you rate the quality of this teammate's work? (1-10)", 10.00);
        Criterion criterion2 = new Criterion("Productivity", "How productive is this teammate? (1-10)", 10.00);
        Criterion criterion3 = new Criterion("Initiative", "How proactive is this teammate? (1-10)", 10.00);
        Criterion criterion4 = new Criterion("Courtesy", "Does this teammate treat others with respect? (1-10)", 10.00);
        Criterion criterion5 = new Criterion("Open-mindedness", "How well does this teammate handle criticism of their work? (1-10)", 10.00);
        Criterion criterion6 = new Criterion("Engagement in meetings", "How is this teammate's performance during meetings? (1-10)", 10.00);
        this.criteria = List.of(criterion1, criterion2, criterion3, criterion4, criterion5, criterion6);
    }

    @Test
    void testFindByCriteria() {
        // Given
        Criterion engagementInMeetings = new Criterion("Engagement in meetings", "How is this teammate's performance during meetings? (1-10)", 10.00);

        Map<String, String> searchCriteria = Map.of("criterion", "meetings");
        PageImpl expectedPage = new PageImpl(List.of(engagementInMeetings));

        given(this.userUtils.getUserCourseId()).willReturn(1);
        given(this.criterionRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(expectedPage);
        // When
        Page<Criterion> result = this.criterionService.findByCriteria(searchCriteria, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCriterion()).isEqualTo("Engagement in meetings");
    }

    @Test
    void testFindCriterionById() {
        // Given
        given(this.criterionRepository.findById(1)).willReturn(Optional.of(this.criteria.get(0)));

        // When
        Criterion result = this.criterionService.findCriterionById(1);

        // Then
        assertThat(result.getCriterion()).isEqualTo("Quality of work");
        assertThat(result.getDescription()).isEqualTo("How do you rate the quality of this teammate's work? (1-10)");
        assertThat(result.getMaxScore()).isEqualTo(10.00);
    }

    @Test
    void testSaveCriterion() {
        // Given
        Criterion newCriterion = new Criterion("Team collaboration", "Assesses the effectiveness of team interactions, including communication, participation, contribution to discussions, willingness to help others, and overall teamwork dynamics. (1-10)", 10.00);
        given(this.criterionRepository.save(newCriterion)).willReturn(newCriterion);

        // When
        Criterion result = this.criterionService.saveCriterion(newCriterion);

        // Then
        assertThat(result.getCriterion()).isEqualTo("Team collaboration");
        assertThat(result.getDescription()).isEqualTo("Assesses the effectiveness of team interactions, including communication, participation, contribution to discussions, willingness to help others, and overall teamwork dynamics. (1-10)");
        assertThat(result.getMaxScore()).isEqualTo(10.00);
    }

    @Test
    void testUpdateCriterion() {
        // Given
        Criterion update = new Criterion("Quality of work (updated)", "How do you rate the quality of this teammate's work? (1-10) (updated)", 9.00);
        given(this.criterionRepository.findById(1)).willReturn(Optional.of(this.criteria.get(0)));
        given(this.criterionRepository.save(any())).willReturn(this.criteria.get(0));

        // When
        Criterion result = this.criterionService.updateCriterion(1, update);

        // Then
        assertThat(result.getCriterion()).isEqualTo(update.getCriterion());
        assertThat(result.getDescription()).isEqualTo(update.getDescription());
        assertThat(result.getMaxScore()).isEqualTo(update.getMaxScore());
    }

    @Test
    void testDeleteCriterion() {
        // Given
        given(this.criterionRepository.findById(1)).willReturn(Optional.of(this.criteria.get(0)));
        doNothing().when(this.criterionRepository).deleteById(1);

        // When
        this.criterionService.deleteCriterion(1);

        // Then
        verify(this.criterionRepository, times(1)).deleteById(1);
    }

}