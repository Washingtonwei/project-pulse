-- Save current session settings and optimize for import
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;

-- Table structure for table `artifact_key_sequence`
CREATE TABLE `artifact_key_sequence` (
  `team_team_id` int NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `next_number` bigint NOT NULL,
  `version` bigint DEFAULT NULL,
  `type` enum('ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RISK','BUSINESS_RULE','CONSTRAINT','DATA_REQUIREMENT','EXTERNAL_INTERFACE_REQUIREMENT','FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','OTHER','POSTCONDITION','PRECONDITION','QUALITY_ATTRIBUTE','RISK','STAKEHOLDER','SUCCESS_METRIC','USER_STORY','USE_CASE','VISION_STATEMENT') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9yvou32dahq4ftyfbne3xbbhe` (`team_team_id`),
  CONSTRAINT `FK9yvou32dahq4ftyfbne3xbbhe` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `artifact_link`
CREATE TABLE `artifact_link` (
  `created_by_id` int DEFAULT NULL,
  `team_team_id` int DEFAULT NULL,
  `updated_by_id` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_artifact_id` bigint DEFAULT NULL,
  `target_artifact_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `notes` longtext,
  `type` enum('DERIVES_FROM','IMPACTS','MITIGATES','MOTIVATES','REALIZES','REFERENCES') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfk27l3wka0ybawed08u7b2k47` (`created_by_id`),
  KEY `FKlmvsyqhc3lp86oxaqadwuulu7` (`source_artifact_id`),
  KEY `FKtjm34sp40pd32928mvbddesj0` (`target_artifact_id`),
  KEY `FKt65eowb7ay5ul9xeftfdg528b` (`team_team_id`),
  KEY `FK3l18b87sm98qb1x0ck70oe83w` (`updated_by_id`),
  CONSTRAINT `FK3l18b87sm98qb1x0ck70oe83w` FOREIGN KEY (`updated_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FKfk27l3wka0ybawed08u7b2k47` FOREIGN KEY (`created_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FKlmvsyqhc3lp86oxaqadwuulu7` FOREIGN KEY (`source_artifact_id`) REFERENCES `requirement_artifact` (`id`),
  CONSTRAINT `FKt65eowb7ay5ul9xeftfdg528b` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`),
  CONSTRAINT `FKtjm34sp40pd32928mvbddesj0` FOREIGN KEY (`target_artifact_id`) REFERENCES `requirement_artifact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `comment`
CREATE TABLE `comment` (
  `author_id` int DEFAULT NULL,
  `comment_index` int DEFAULT NULL,
  `comment_thread_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `content` longtext,
  PRIMARY KEY (`id`),
  KEY `FKo1gq8i4ywpw214oq700fqhh7j` (`author_id`),
  KEY `FKryuhdv7b3hva1nt77ndwniq9t` (`comment_thread_id`),
  CONSTRAINT `FKo1gq8i4ywpw214oq700fqhh7j` FOREIGN KEY (`author_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FKryuhdv7b3hva1nt77ndwniq9t` FOREIGN KEY (`comment_thread_id`) REFERENCES `comment_thread` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `comment_thread`
CREATE TABLE `comment_thread` (
  `created_by_id` int DEFAULT NULL,
  `team_team_id` int DEFAULT NULL,
  `updated_by_id` int DEFAULT NULL,
  `artifact_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  `document_section_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `status` enum('OPEN','RESOLVED') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmtynrmdah22h09nshi8css78c` (`artifact_id`),
  KEY `FK6xj7onmcxp9kbuoy0tuy9t7kx` (`created_by_id`),
  KEY `FKsosbiyjb50atlkgqkcu74tv2d` (`document_id`),
  KEY `FK95pn34vdy92lxr4utp4bmnq8x` (`document_section_id`),
  KEY `FKl1p6jsuyiqf06nvxda513y5ye` (`team_team_id`),
  KEY `FK5br1qn14ol7adu9e7ycc58nuq` (`updated_by_id`),
  CONSTRAINT `FK5br1qn14ol7adu9e7ycc58nuq` FOREIGN KEY (`updated_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FK6xj7onmcxp9kbuoy0tuy9t7kx` FOREIGN KEY (`created_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FK95pn34vdy92lxr4utp4bmnq8x` FOREIGN KEY (`document_section_id`) REFERENCES `document_section` (`id`),
  CONSTRAINT `FKl1p6jsuyiqf06nvxda513y5ye` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`),
  CONSTRAINT `FKmtynrmdah22h09nshi8css78c` FOREIGN KEY (`artifact_id`) REFERENCES `requirement_artifact` (`id`),
  CONSTRAINT `FKsosbiyjb50atlkgqkcu74tv2d` FOREIGN KEY (`document_id`) REFERENCES `requirement_document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `document_section`
CREATE TABLE `document_section` (
  `created_by_id` int DEFAULT NULL,
  `section_index` int DEFAULT NULL,
  `updated_by_id` int DEFAULT NULL,
  `version` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `section_key` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `content` longtext,
  `guidance` longtext,
  `type` enum('LIST','RICH_TEXT') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKopy1pibbrjq6912migimrxn4q` (`created_by_id`),
  KEY `FKsupkor7l05adux2149nk6hr61` (`document_id`),
  KEY `FKp7m4ugv3cnoq4lkpsj0rr7kb9` (`updated_by_id`),
  CONSTRAINT `FKopy1pibbrjq6912migimrxn4q` FOREIGN KEY (`created_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FKp7m4ugv3cnoq4lkpsj0rr7kb9` FOREIGN KEY (`updated_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FKsupkor7l05adux2149nk6hr61` FOREIGN KEY (`document_id`) REFERENCES `requirement_document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `document_section_lock`
CREATE TABLE `document_section_lock` (
  `locked_by_id` int DEFAULT NULL,
  `version` int DEFAULT NULL,
  `document_section_id` bigint NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `locked_at` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrb445eus6nqmm60wpywpq56rg` (`document_section_id`),
  KEY `FKpl9fmr2r14xv6va55udh5nv2o` (`locked_by_id`),
  CONSTRAINT `FK903qobnvgl1ikeb49159pi2gj` FOREIGN KEY (`document_section_id`) REFERENCES `document_section` (`id`),
  CONSTRAINT `FKpl9fmr2r14xv6va55udh5nv2o` FOREIGN KEY (`locked_by_id`) REFERENCES `peer_evaluation_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `requirement_artifact`
CREATE TABLE `requirement_artifact` (
  `artifact_index` int DEFAULT NULL,
  `created_by_id` int DEFAULT NULL,
  `team_team_id` int DEFAULT NULL,
  `updated_by_id` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `source_document_section_id` bigint DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `artifact_key` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `content` longtext,
  `notes` longtext,
  `priority` enum('CRITICAL','HIGH','LOW','MEDIUM') DEFAULT NULL,
  `type` enum('ASSUMPTION','BUSINESS_OBJECTIVE','BUSINESS_OPPORTUNITY','BUSINESS_PROBLEM','BUSINESS_RISK','BUSINESS_RULE','CONSTRAINT','DATA_REQUIREMENT','EXTERNAL_INTERFACE_REQUIREMENT','FEATURE','FUNCTIONAL_REQUIREMENT','GLOSSARY_TERM','OTHER','POSTCONDITION','PRECONDITION','QUALITY_ATTRIBUTE','RISK','STAKEHOLDER','SUCCESS_METRIC','USER_STORY','USE_CASE','VISION_STATEMENT') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8hc7c24926q0j32qwc13lc6cb` (`created_by_id`),
  KEY `FKpfiuqfmv3psyacg13htervxj6` (`source_document_section_id`),
  KEY `FKipwvw342vl86gt02me9k0vsse` (`team_team_id`),
  KEY `FK5ljsaep9u4qyrgf794vkbctbl` (`updated_by_id`),
  CONSTRAINT `FK5ljsaep9u4qyrgf794vkbctbl` FOREIGN KEY (`updated_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FK8hc7c24926q0j32qwc13lc6cb` FOREIGN KEY (`created_by_id`) REFERENCES `peer_evaluation_user` (`id`),
  CONSTRAINT `FKipwvw342vl86gt02me9k0vsse` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`),
  CONSTRAINT `FKpfiuqfmv3psyacg13htervxj6` FOREIGN KEY (`source_document_section_id`) REFERENCES `document_section` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `requirement_document`
CREATE TABLE `requirement_document` (
  `team_team_id` int DEFAULT NULL,
  `version` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) DEFAULT NULL,
  `document_key` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `status` enum('DRAFT','RETURNED','SUBMITTED') DEFAULT NULL,
  `type` enum('BUSINESS_RULES','GLOSSARY','SRS','USER_STORIES','USE_CASES','VISION_SCOPE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKex4umeqic43w8thr6f7au59y4` (`team_team_id`),
  CONSTRAINT `FKex4umeqic43w8thr6f7au59y4` FOREIGN KEY (`team_team_id`) REFERENCES `team` (`team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case`
CREATE TABLE `use_case` (
  `version` int DEFAULT NULL,
  `artifact_id` bigint NOT NULL,
  `primary_actor_id` bigint DEFAULT NULL,
  `use_case_trigger` longtext,
  PRIMARY KEY (`artifact_id`),
  KEY `FKby3rbcs00t8s24y2yvju5hn1x` (`primary_actor_id`),
  CONSTRAINT `FK93j76hn9kmnx03486akmbw08a` FOREIGN KEY (`artifact_id`) REFERENCES `requirement_artifact` (`id`),
  CONSTRAINT `FKby3rbcs00t8s24y2yvju5hn1x` FOREIGN KEY (`primary_actor_id`) REFERENCES `requirement_artifact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case_extension`
CREATE TABLE `use_case_extension` (
  `extension_index` int DEFAULT NULL,
  `resume_step_no` int DEFAULT NULL,
  `base_step_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(255) DEFAULT NULL,
  `condition_text` longtext,
  `extension_exit` enum('END_FAILURE','END_SUCCESS','RESUME') DEFAULT NULL,
  `kind` enum('ALTERNATE','EXCEPTION') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKghc6ff42eittvpjhr4p7vfwi8` (`base_step_id`),
  CONSTRAINT `FKghc6ff42eittvpjhr4p7vfwi8` FOREIGN KEY (`base_step_id`) REFERENCES `use_case_main_step` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case_extension_step`
CREATE TABLE `use_case_extension_step` (
  `extension_step_index` int DEFAULT NULL,
  `extension_id` bigint DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actor` varchar(255) DEFAULT NULL,
  `action_text` longtext,
  PRIMARY KEY (`id`),
  KEY `FKg0ve3dvmytoxmdmwnutxkixvb` (`extension_id`),
  CONSTRAINT `FKg0ve3dvmytoxmdmwnutxkixvb` FOREIGN KEY (`extension_id`) REFERENCES `use_case_extension` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case_main_step`
CREATE TABLE `use_case_main_step` (
  `main_step_index` int DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `use_case_artifact_id` bigint DEFAULT NULL,
  `actor` varchar(255) DEFAULT NULL,
  `action_text` longtext,
  PRIMARY KEY (`id`),
  KEY `FKavurbeiwfm8yy9pyxtgxvat34` (`use_case_artifact_id`),
  CONSTRAINT `FKavurbeiwfm8yy9pyxtgxvat34` FOREIGN KEY (`use_case_artifact_id`) REFERENCES `use_case` (`artifact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case_postcondition`
CREATE TABLE `use_case_postcondition` (
  `postcondition_artifact_id` bigint NOT NULL,
  `use_case_id` bigint NOT NULL,
  PRIMARY KEY (`postcondition_artifact_id`,`use_case_id`),
  KEY `FK3i8xwyaajf5pgjxvy8d2j4x5s` (`use_case_id`),
  CONSTRAINT `FK3i8xwyaajf5pgjxvy8d2j4x5s` FOREIGN KEY (`use_case_id`) REFERENCES `use_case` (`artifact_id`),
  CONSTRAINT `FKavn1ufxtfgrwvnlwos92hniyo` FOREIGN KEY (`postcondition_artifact_id`) REFERENCES `requirement_artifact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case_precondition`
CREATE TABLE `use_case_precondition` (
  `precondition_artifact_id` bigint NOT NULL,
  `use_case_id` bigint NOT NULL,
  PRIMARY KEY (`precondition_artifact_id`,`use_case_id`),
  KEY `FK86gruuyappjqyd565esquxqrv` (`use_case_id`),
  CONSTRAINT `FK86gruuyappjqyd565esquxqrv` FOREIGN KEY (`use_case_id`) REFERENCES `use_case` (`artifact_id`),
  CONSTRAINT `FKmw70ecnof4ry4unj25evv9hmd` FOREIGN KEY (`precondition_artifact_id`) REFERENCES `requirement_artifact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Table structure for table `use_case_secondary_actor`
CREATE TABLE `use_case_secondary_actor` (
  `stakeholder_artifact_id` bigint NOT NULL,
  `use_case_id` bigint NOT NULL,
  PRIMARY KEY (`stakeholder_artifact_id`,`use_case_id`),
  KEY `FK7jkv135phrnchoaly2cccyb0c` (`use_case_id`),
  CONSTRAINT `FK4bdeaheefktd5pcsaf7m5yjer` FOREIGN KEY (`stakeholder_artifact_id`) REFERENCES `requirement_artifact` (`id`),
  CONSTRAINT `FK7jkv135phrnchoaly2cccyb0c` FOREIGN KEY (`use_case_id`) REFERENCES `use_case` (`artifact_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Restore original session settings
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
