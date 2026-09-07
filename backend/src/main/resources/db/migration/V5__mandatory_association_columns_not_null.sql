-- Enforce, in the schema, the to-one associations that are mandatory in the domain.
-- Each column below backs a @ManyToOne(optional = false) field, so Hibernate already refuses to
-- write a null there; this closes the gap for anything that reaches the tables outside JPA
-- (a manual INSERT, a data-repair script, a batch job).
--
-- No data migration is required: every column below was verified to hold zero nulls in both
-- staging and prod before this migration was written.
--
-- Deliberately excluded: peer_evaluation_user.section_section_id, which backs Student.section.
-- Student and Instructor share that table under single-table inheritance (dtype discriminator),
-- and the column is legitimately null for every instructor row, so it cannot be NOT NULL.
-- Student.section stays enforced at the application layer by @ManyToOne(optional = false) alone.

ALTER TABLE `activity`
    MODIFY `team_team_id` int NOT NULL,
    MODIFY `student_id` int NOT NULL;

ALTER TABLE `course`
    MODIFY `course_admin_id` int NOT NULL;

ALTER TABLE `section`
    MODIFY `course_course_id` int NOT NULL;

ALTER TABLE `team`
    MODIFY `section_section_id` int NOT NULL;

ALTER TABLE `criterion`
    MODIFY `course_course_id` int NOT NULL;

ALTER TABLE `rubric`
    MODIFY `course_course_id` int NOT NULL;

ALTER TABLE `peer_evaluation`
    MODIFY `evaluator_id` int NOT NULL,
    MODIFY `evaluatee_id` int NOT NULL;

ALTER TABLE `rating`
    MODIFY `criterion_criterion_id` int NOT NULL,
    MODIFY `peer_evaluation_peer_evaluation_id` int NOT NULL;
