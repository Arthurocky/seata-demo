CREATE DATABASE IF NOT EXISTS `seata_demo_account` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `seata_demo_account`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `seata_demo_account`.`tb_account`;
CREATE TABLE `seata_demo_account`.`tb_account`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255),
  `money` int(11) UNSIGNED NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4;

INSERT INTO `seata_demo_account`.`tb_account` VALUES (1, 'user202103032042012', 1000);

DROP TABLE IF EXISTS `seata_demo_account`.`undo_log`;
CREATE TABLE `seata_demo_account`.`undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

DROP TABLE IF EXISTS `seata_demo_account`.`tb_account_freeze`;
CREATE TABLE `seata_demo_account`.`tb_account_freeze`  (
  `xid` varchar(128) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `freeze_money` int(11) UNSIGNED DEFAULT 0,
  `state` int(1) NULL DEFAULT NULL COMMENT '事务状态，0:try，1:confirm，2:cancel',
  PRIMARY KEY (`xid`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4;
SET FOREIGN_KEY_CHECKS = 1;


CREATE DATABASE IF NOT EXISTS `seata_demo_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `seata_demo_order`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `seata_demo_order`.`tb_order`;
CREATE TABLE `seata_demo_order`.`tb_order`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(255),
  `commodity_code` varchar(255),
  `count` int(11) UNSIGNED DEFAULT 0,
  `money` int(11) UNSIGNED DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4;

DROP TABLE IF EXISTS `seata_demo_order`.`undo_log`;
CREATE TABLE `seata_demo_order`.`undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
SET FOREIGN_KEY_CHECKS = 1;


CREATE DATABASE IF NOT EXISTS `seata_demo_storage` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `seata_demo_storage`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `seata_demo_storage`.`tb_storage`;
CREATE TABLE `seata_demo_storage`.`tb_storage`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `commodity_code` varchar(20) NOT NULL,
  `count` int(11) UNSIGNED DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_CC`(`commodity_code`)
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = COMPACT;

INSERT INTO `seata_demo_storage`.`tb_storage` VALUES (1, '100202003032041', 10);

DROP TABLE IF EXISTS `seata_demo_storage`.`undo_log`;
CREATE TABLE `seata_demo_storage`.`undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)      NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
SET FOREIGN_KEY_CHECKS = 1;

-- 恢复数据
UPDATE SEATA_DEMO_STORAGE.TB_STORAGE SET COUNT=10 WHERE ID=1;
TRUNCATE seata_demo_order.tb_order;
UPDATE seata_demo_account.tb_account SET MONEY=1000 WHERE ID=1;
