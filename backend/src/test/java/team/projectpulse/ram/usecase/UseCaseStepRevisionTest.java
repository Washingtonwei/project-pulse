package team.projectpulse.ram.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UseCase#reviseMainSteps}: how submitted steps pair with the use case's own steps, and the
 * same rule one and two levels down. The "own" entities carry ids as if they had been loaded; the submitted ones
 * stand in for what the converters build from a request body.
 */
class UseCaseStepRevisionTest {

    UseCase useCase;
    UseCaseMainStep stepA;
    UseCaseMainStep stepB;
    UseCaseMainStep stepC;
    UseCaseExtension extensionOfB;
    UseCaseExtensionStep extensionStepOfB;


    @BeforeEach
    void setUp() {
        this.useCase = new UseCase();
        this.stepA = new UseCaseMainStep(1L, "Student", "A");
        this.stepB = new UseCaseMainStep(2L, "System", "B");
        this.stepC = new UseCaseMainStep(3L, "System", "C");
        this.useCase.addMainStep(this.stepA);
        this.useCase.addMainStep(this.stepB);
        this.useCase.addMainStep(this.stepC);

        this.extensionOfB = new UseCaseExtension(10L, "B fails", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        this.extensionStepOfB = new UseCaseExtensionStep(100L, "System", "Recover");
        this.extensionOfB.addStep(this.extensionStepOfB);
        this.stepB.addExtension(this.extensionOfB);
    }

    @Test
    @DisplayName("A submitted step with one of the use case's own ids updates that step in place")
    void testPairedStepIsUpdatedInPlace() {
        this.useCase.reviseMainSteps(List.of(submitted(1L, "A reworded"), submitted(2L, "B"), submitted(3L, "C")));

        assertThat(this.useCase.getMainSteps()).containsExactly(this.stepA, this.stepB, this.stepC);
        assertThat(this.stepA.getActionText()).isEqualTo("A reworded");
        assertThat(this.stepA.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Submitting the use case unchanged leaves every step where it was")
    void testUnchangedSubmissionLeavesEverythingInPlace() {
        // Unchanged means the whole tree, extensions included, exactly as the editor sends it back.
        UseCaseMainStep submittedB = submitted(2L, "B");
        UseCaseExtension submittedExtension = new UseCaseExtension(10L, "B fails", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        submittedExtension.addStep(new UseCaseExtensionStep(100L, "System", "Recover"));
        submittedB.addExtension(submittedExtension);

        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submittedB, submitted(3L, "C")));

        assertThat(this.useCase.getMainSteps()).containsExactly(this.stepA, this.stepB, this.stepC);
        assertThat(this.stepB.getExtensions()).containsExactly(this.extensionOfB);
        assertThat(this.extensionOfB.getSteps()).containsExactly(this.extensionStepOfB);
    }

    @Test
    @DisplayName("A submitted step without an id is inserted as a new step at its submitted position")
    void testStepWithoutIdIsInsertedAtItsPosition() {
        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submitted(null, "New"), submitted(2L, "B"), submitted(3L, "C")));

        List<UseCaseMainStep> steps = this.useCase.getMainSteps();
        assertThat(steps).hasSize(4);
        assertThat(steps.get(0)).isSameAs(this.stepA);
        assertThat(steps.get(1).getId()).isNull();
        assertThat(steps.get(1).getActionText()).isEqualTo("New");
        assertThat(steps.get(1).getUseCase()).isSameAs(this.useCase);
        assertThat(steps.subList(2, 4)).containsExactly(this.stepB, this.stepC);
    }

    @Test
    @DisplayName("An id the use case does not own becomes a new step; the submitted object itself is never attached")
    void testForeignIdBecomesAFreshCopy() {
        UseCaseMainStep foreign = submitted(999L, "Stolen");

        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submitted(2L, "B"), submitted(3L, "C"), foreign));

        UseCaseMainStep last = this.useCase.getMainSteps().get(3);
        assertThat(last).isNotSameAs(foreign);
        assertThat(last.getId()).isNull();
        assertThat(last.getActionText()).isEqualTo("Stolen");
    }

    @Test
    @DisplayName("An id submitted twice pairs once; the second occurrence becomes a new step")
    void testRepeatedIdPairsOnlyOnce() {
        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submitted(1L, "A again"), submitted(2L, "B"), submitted(3L, "C")));

        List<UseCaseMainStep> steps = this.useCase.getMainSteps();
        assertThat(steps).hasSize(4);
        assertThat(steps.get(0)).isSameAs(this.stepA);
        assertThat(steps.get(1)).isNotSameAs(this.stepA);
        assertThat(steps.get(1).getId()).isNull();
        assertThat(steps.get(1).getActionText()).isEqualTo("A again");
    }

    @Test
    @DisplayName("Own steps the submission omits are removed, and the rest keep their order")
    void testOmittedStepIsRemoved() {
        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submitted(3L, "C")));

        assertThat(this.useCase.getMainSteps()).containsExactly(this.stepA, this.stepC);
    }

    @Test
    @DisplayName("Deleting every step leaves an empty main success scenario")
    void testAllStepsCanBeRemoved() {
        this.useCase.reviseMainSteps(List.of());

        assertThat(this.useCase.getMainSteps()).isEmpty();
    }

    @Test
    @DisplayName("The order follows the submission, moving existing steps rather than replacing them")
    void testOrderFollowsTheSubmission() {
        this.useCase.reviseMainSteps(List.of(submitted(3L, "C"), submitted(1L, "A"), submitted(2L, "B")));

        assertThat(this.useCase.getMainSteps()).containsExactly(this.stepC, this.stepA, this.stepB);
    }

    @Test
    @DisplayName("A paired extension is updated in place, keeping label and resumeStepNo, which no request carries")
    void testPairedExtensionIsUpdatedInPlace() {
        this.extensionOfB.setLabel("2a");
        this.extensionOfB.setResumeStepNo(3);
        UseCaseMainStep submittedB = submitted(2L, "B");
        UseCaseExtension submittedExtension = new UseCaseExtension(10L, "B fails badly", ExtensionKind.ALTERNATE, ExtensionExit.END_FAILURE);
        submittedExtension.addStep(new UseCaseExtensionStep(100L, "Student", "Recover by hand"));
        submittedB.addExtension(submittedExtension);

        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submittedB, submitted(3L, "C")));

        assertThat(this.stepB.getExtensions()).containsExactly(this.extensionOfB);
        assertThat(this.extensionOfB.getConditionText()).isEqualTo("B fails badly");
        assertThat(this.extensionOfB.getKind()).isEqualTo(ExtensionKind.ALTERNATE);
        assertThat(this.extensionOfB.getExtensionExit()).isEqualTo(ExtensionExit.END_FAILURE);
        assertThat(this.extensionOfB.getLabel()).isEqualTo("2a");
        assertThat(this.extensionOfB.getResumeStepNo()).isEqualTo(3);
        assertThat(this.extensionOfB.getSteps()).containsExactly(this.extensionStepOfB);
        assertThat(this.extensionStepOfB.getActor()).isEqualTo("Student");
        assertThat(this.extensionStepOfB.getActionText()).isEqualTo("Recover by hand");
    }

    @Test
    @DisplayName("An extension id submitted under a different step pairs with nothing there and becomes new")
    void testExtensionIdIsPairedOnlyWithinItsOwnStep() {
        UseCaseMainStep submittedA = submitted(1L, "A");
        submittedA.addExtension(new UseCaseExtension(10L, "Borrowed from B", ExtensionKind.EXCEPTION, ExtensionExit.RESUME));
        UseCaseMainStep submittedB = submitted(2L, "B");
        submittedB.addExtension(new UseCaseExtension(10L, "B fails", ExtensionKind.EXCEPTION, ExtensionExit.RESUME));

        this.useCase.reviseMainSteps(List.of(submittedA, submittedB, submitted(3L, "C")));

        UseCaseExtension extensionOfA = this.stepA.getExtensions().get(0);
        assertThat(extensionOfA).isNotSameAs(this.extensionOfB);
        assertThat(extensionOfA.getId()).isNull();
        assertThat(extensionOfA.getBaseStep()).isSameAs(this.stepA);
        assertThat(this.stepB.getExtensions()).containsExactly(this.extensionOfB);
        assertThat(this.extensionOfB.getBaseStep()).isSameAs(this.stepB);
    }

    @Test
    @DisplayName("Omitting a step's extension removes it, and a new extension gets fresh copies of its steps")
    void testExtensionsAreRemovedAndAdded() {
        UseCaseMainStep submittedC = submitted(3L, "C");
        UseCaseExtension newExtension = new UseCaseExtension(null, "C fails", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        UseCaseExtensionStep newExtensionStep = new UseCaseExtensionStep(100L, "System", "Borrowed step id");
        newExtension.addStep(newExtensionStep);
        submittedC.addExtension(newExtension);

        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submitted(2L, "B"), submittedC));

        assertThat(this.stepB.getExtensions()).isEmpty();
        UseCaseExtension added = this.stepC.getExtensions().get(0);
        assertThat(added.getId()).isNull();
        UseCaseExtensionStep addedStep = added.getSteps().get(0);
        assertThat(addedStep).isNotSameAs(newExtensionStep).isNotSameAs(this.extensionStepOfB);
        assertThat(addedStep.getId()).isNull();
        assertThat(addedStep.getExtension()).isSameAs(added);
    }

    @Test
    @DisplayName("Extension steps are inserted and removed within their extension like main steps")
    void testExtensionStepsAreRevisedInPlace() {
        UseCaseMainStep submittedB = submitted(2L, "B");
        UseCaseExtension submittedExtension = new UseCaseExtension(10L, "B fails", ExtensionKind.EXCEPTION, ExtensionExit.RESUME);
        submittedExtension.addStep(new UseCaseExtensionStep(null, "System", "Inserted first"));
        submittedExtension.addStep(new UseCaseExtensionStep(100L, "System", "Recover"));
        submittedB.addExtension(submittedExtension);

        this.useCase.reviseMainSteps(List.of(submitted(1L, "A"), submittedB, submitted(3L, "C")));

        List<UseCaseExtensionStep> steps = this.extensionOfB.getSteps();
        assertThat(steps).hasSize(2);
        assertThat(steps.get(0).getId()).isNull();
        assertThat(steps.get(0).getActionText()).isEqualTo("Inserted first");
        assertThat(steps.get(1)).isSameAs(this.extensionStepOfB);
    }


    private static UseCaseMainStep submitted(Long id, String actionText) {
        return new UseCaseMainStep(id, "System", actionText);
    }

}
