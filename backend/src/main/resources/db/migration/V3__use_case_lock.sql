-- Table structure for table `use_case_lock`
CREATE TABLE `use_case_lock` (
  `locked_by_id` int DEFAULT NULL,
  `version` int DEFAULT NULL,
  `use_case_id` bigint NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `locked_at` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_use_case_lock_use_case` (`use_case_id`),
  KEY `FK_use_case_lock_locked_by` (`locked_by_id`),
  CONSTRAINT `FK_use_case_lock_use_case` FOREIGN KEY (`use_case_id`) REFERENCES `use_case` (`artifact_id`),
  CONSTRAINT `FK_use_case_lock_locked_by` FOREIGN KEY (`locked_by_id`) REFERENCES `peer_evaluation_user` (`id`)
);
