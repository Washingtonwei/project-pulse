-- Save current session settings and optimize for import
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;

-- Table structure for table `activity`
CREATE TABLE `activity` (
                            `activity_id` int NOT NULL AUTO_INCREMENT,
                            `actual_hours` double DEFAULT NULL,
                            `category` tinyint DEFAULT NULL,
                            `planned_hours` double DEFAULT NULL,
                            `status` tinyint DEFAULT NULL,
                            `student_id` int DEFAULT NULL,
                            `team_team_id` int DEFAULT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `activity` varchar(255) DEFAULT NULL,
                            `comments` varchar(255) DEFAULT NULL,
                            `description` varchar(255) DEFAULT NULL,
                            `week` varchar(255) DEFAULT NULL,
                            PRIMARY KEY (`activity_id`),
                            KEY `FKa5w9ku6jx44543pt6nok01dft` (`student_id`),
                            KEY `FKracd6lriw4oaxdohvxlnr5jyl` (`team_team_id`),
                            CONSTRAINT `FKa5w9ku6jx44543pt6nok01dft` FOREIGN KEY (`student_id`) REFERENCES `peer_evaluation_user` (`id`),
                            CONSTRAINT `FKracd6lriw4oaxdohvxlnr5jyl` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`),
                            CONSTRAINT `activity_chk_1` CHECK ((`category` between 0 and 10)),
                            CONSTRAINT `activity_chk_2` CHECK ((`status` between 0 and 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `course`
CREATE TABLE `course` (
                          `course_admin_id` int DEFAULT NULL,
                          `course_id` int NOT NULL AUTO_INCREMENT,
                          `course_description` varchar(255) DEFAULT NULL,
                          `course_name` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`course_id`),
                          KEY `FKlb1wcxwkh479xpkq5x9k31r59` (`course_admin_id`),
                          CONSTRAINT `FKlb1wcxwkh479xpkq5x9k31r59` FOREIGN KEY (`course_admin_id`) REFERENCES `peer_evaluation_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `course_instructor`
CREATE TABLE `course_instructor` (
                                     `course_id` int NOT NULL,
                                     `instructor_id` int NOT NULL,
                                     PRIMARY KEY (`course_id`,`instructor_id`),
                                     KEY `FKq67rv0bdqr05g8bej32pmilq7` (`instructor_id`),
                                     CONSTRAINT `FKeqej22fgwa29i98ucd9x9ycie` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
                                     CONSTRAINT `FKq67rv0bdqr05g8bej32pmilq7` FOREIGN KEY (`instructor_id`) REFERENCES `peer_evaluation_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `criterion`
CREATE TABLE `criterion` (
                             `course_course_id` int DEFAULT NULL,
                             `criterion_id` int NOT NULL AUTO_INCREMENT,
                             `max_score` double NOT NULL,
                             `criterion` varchar(255) NOT NULL,
                             `description` varchar(255) NOT NULL,
                             PRIMARY KEY (`criterion_id`),
                             KEY `FKsm5rifji7sn3a2cwe55lgekxc` (`course_course_id`),
                             CONSTRAINT `FKsm5rifji7sn3a2cwe55lgekxc` FOREIGN KEY (`course_course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Table structure for table `peer_evaluation`
--

CREATE TABLE `peer_evaluation` (
                                   `evaluatee_id` int DEFAULT NULL,
                                   `evaluator_id` int DEFAULT NULL,
                                   `peer_evaluation_id` int NOT NULL AUTO_INCREMENT,
                                   `total_score` double DEFAULT NULL,
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   `private_comment` varchar(255) DEFAULT NULL,
                                   `public_comment` varchar(255) DEFAULT NULL,
                                   `week` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`peer_evaluation_id`),
                                   KEY `FKrk4h1o4pu3ei89ypwl23swbd3` (`evaluatee_id`),
                                   KEY `FK1pflvjhop7lfak6hwnh2ldk9v` (`evaluator_id`),
                                   CONSTRAINT `FK1pflvjhop7lfak6hwnh2ldk9v` FOREIGN KEY (`evaluator_id`) REFERENCES `peer_evaluation_user` (`id`),
                                   CONSTRAINT `FKrk4h1o4pu3ei89ypwl23swbd3` FOREIGN KEY (`evaluatee_id`) REFERENCES `peer_evaluation_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `peer_evaluation_user`
CREATE TABLE `peer_evaluation_user` (
                                        `default_course_course_id` int DEFAULT NULL,
                                        `default_section_section_id` int DEFAULT NULL,
                                        `enabled` bit(1) NOT NULL,
                                        `id` int NOT NULL AUTO_INCREMENT,
                                        `section_section_id` int DEFAULT NULL,
                                        `team_team_id` int DEFAULT NULL,
                                        `dtype` varchar(31) NOT NULL,
                                        `email` varchar(255) NOT NULL,
                                        `first_name` varchar(255) NOT NULL,
                                        `last_name` varchar(255) NOT NULL,
                                        `password` varchar(255) NOT NULL,
                                        `roles` varchar(255) DEFAULT NULL,
                                        `username` varchar(255) NOT NULL,
                                        PRIMARY KEY (`id`),
                                        KEY `FKrakyek3pl9rvokurpa42ims1s` (`default_course_course_id`),
                                        KEY `FKocua9ortgt5j7vlts2n0y2rsi` (`default_section_section_id`),
                                        KEY `FK6kob0tbp2e39jtqsueb9wmrq8` (`section_section_id`),
                                        KEY `FKs0r69hgtk6ge33ujevappqahk` (`team_team_id`),
                                        CONSTRAINT `FK6kob0tbp2e39jtqsueb9wmrq8` FOREIGN KEY (`section_section_id`) REFERENCES `section` (`section_id`),
                                        CONSTRAINT `FKocua9ortgt5j7vlts2n0y2rsi` FOREIGN KEY (`default_section_section_id`) REFERENCES `section` (`section_id`),
                                        CONSTRAINT `FKrakyek3pl9rvokurpa42ims1s` FOREIGN KEY (`default_course_course_id`) REFERENCES `course` (`course_id`),
                                        CONSTRAINT `FKs0r69hgtk6ge33ujevappqahk` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `rating`
CREATE TABLE `rating` (
                          `actual_score` double DEFAULT NULL,
                          `criterion_criterion_id` int DEFAULT NULL,
                          `peer_evaluation_peer_evaluation_id` int DEFAULT NULL,
                          `rating_id` int NOT NULL AUTO_INCREMENT,
                          PRIMARY KEY (`rating_id`),
                          KEY `FKnasjo68tk8iy6to628v1v8xxg` (`criterion_criterion_id`),
                          KEY `FK2sauw46awdhv4mx0t7qi3j0fv` (`peer_evaluation_peer_evaluation_id`),
                          CONSTRAINT `FK2sauw46awdhv4mx0t7qi3j0fv` FOREIGN KEY (`peer_evaluation_peer_evaluation_id`) REFERENCES `peer_evaluation` (`peer_evaluation_id`),
                          CONSTRAINT `FKnasjo68tk8iy6to628v1v8xxg` FOREIGN KEY (`criterion_criterion_id`) REFERENCES `criterion` (`criterion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `reset_password_token`
CREATE TABLE `reset_password_token` (
                                        `expiry_date` datetime(6) DEFAULT NULL,
                                        `email` varchar(255) NOT NULL,
                                        `token` varchar(255) DEFAULT NULL,
                                        PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `rubric`
CREATE TABLE `rubric` (
                          `course_course_id` int DEFAULT NULL,
                          `rubric_id` int NOT NULL AUTO_INCREMENT,
                          `rubric_name` varchar(255) NOT NULL,
                          PRIMARY KEY (`rubric_id`),
                          KEY `FKtggmi92tqjgxlihyhbg15xqd5` (`course_course_id`),
                          CONSTRAINT `FKtggmi92tqjgxlihyhbg15xqd5` FOREIGN KEY (`course_course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `rubric_criterion`
CREATE TABLE `rubric_criterion` (
                                    `criterion_id` int NOT NULL,
                                    `rubric_id` int NOT NULL,
                                    PRIMARY KEY (`criterion_id`,`rubric_id`),
                                    KEY `FK5yqdms3yo15fswpve6wxjig16` (`rubric_id`),
                                    CONSTRAINT `FK5yqdms3yo15fswpve6wxjig16` FOREIGN KEY (`rubric_id`) REFERENCES `rubric` (`rubric_id`),
                                    CONSTRAINT `FKgmvn7r0e5v75hdhkk1bh720p3` FOREIGN KEY (`criterion_id`) REFERENCES `criterion` (`criterion_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `section`
CREATE TABLE `section` (
                           `course_course_id` int DEFAULT NULL,
                           `end_date` date DEFAULT NULL,
                           `rubric_rubric_id` int DEFAULT NULL,
                           `section_id` int NOT NULL AUTO_INCREMENT,
                           `start_date` date DEFAULT NULL,
                           `section_name` varchar(255) DEFAULT NULL,
                           `war_weekly_due_day` varchar(20) DEFAULT NULL,
                           `war_due_time` time DEFAULT NULL,
                           `peer_evaluation_weekly_due_day` varchar(20) DEFAULT NULL,
                           `peer_evaluation_due_time` time DEFAULT NULL,
                           `is_active` tinyint(1) DEFAULT '0',
                           PRIMARY KEY (`section_id`),
                           KEY `FKcaob2rpl5w0q76503ake08qdn` (`course_course_id`),
                           KEY `FK3ddg24nw0m8qqfn3tqelv9s2y` (`rubric_rubric_id`),
                           CONSTRAINT `FK3ddg24nw0m8qqfn3tqelv9s2y` FOREIGN KEY (`rubric_rubric_id`) REFERENCES `rubric` (`rubric_id`),
                           CONSTRAINT `FKcaob2rpl5w0q76503ake08qdn` FOREIGN KEY (`course_course_id`) REFERENCES `course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `section_active_weeks`
CREATE TABLE `section_active_weeks` (
                                        `my_row_id` bigint unsigned NOT NULL AUTO_INCREMENT INVISIBLE,
                                        `section_section_id` int NOT NULL,
                                        `active_weeks` varchar(255) DEFAULT NULL,
                                        PRIMARY KEY (`my_row_id`),
                                        KEY `FK1d092prm0mkywkto40cplsmm5` (`section_section_id`),
                                        CONSTRAINT `FK1d092prm0mkywkto40cplsmm5` FOREIGN KEY (`section_section_id`) REFERENCES `section` (`section_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `section_instructor`
CREATE TABLE `section_instructor` (
                                      `instructor_id` int NOT NULL,
                                      `section_id` int NOT NULL,
                                      PRIMARY KEY (`instructor_id`,`section_id`),
                                      KEY `FKpf29kgm7o3gnc40yqthc9fyov` (`section_id`),
                                      CONSTRAINT `FKi4e06rsa3d14b5f1ekn2cxp91` FOREIGN KEY (`instructor_id`) REFERENCES `peer_evaluation_user` (`id`),
                                      CONSTRAINT `FKpf29kgm7o3gnc40yqthc9fyov` FOREIGN KEY (`section_id`) REFERENCES `section` (`section_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `team`
CREATE TABLE `team` (
                        `instructor_id` int DEFAULT NULL,
                        `section_section_id` int DEFAULT NULL,
                        `team_id` int NOT NULL AUTO_INCREMENT,
                        `description` varchar(255) DEFAULT NULL,
                        `team_name` varchar(255) DEFAULT NULL,
                        `team_website_url` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`team_id`),
                        KEY `FKc7peb88quevji666ynwc6lvjx` (`instructor_id`),
                        KEY `FKpefqxvyo2oneigiwjniorbshe` (`section_section_id`),
                        CONSTRAINT `FKc7peb88quevji666ynwc6lvjx` FOREIGN KEY (`instructor_id`) REFERENCES `peer_evaluation_user` (`id`),
                        CONSTRAINT `FKpefqxvyo2oneigiwjniorbshe` FOREIGN KEY (`section_section_id`) REFERENCES `section` (`section_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `user_invitation`
CREATE TABLE `user_invitation` (
                                   `course_id` int DEFAULT NULL,
                                   `section_id` int DEFAULT NULL,
                                   `email` varchar(255) NOT NULL,
                                   `role` varchar(255) DEFAULT NULL,
                                   `token` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Restore original session settings
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;