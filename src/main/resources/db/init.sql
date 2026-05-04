-- ============================================
-- BookSys 数据库初始化脚本
-- 数据库名: cov
-- 字符集: utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS `cov`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `cov`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(32)  NOT NULL                COMMENT '用户名',
  `password`    VARCHAR(128) NOT NULL                COMMENT '密码(MD5)',
  `nickname`    VARCHAR(32)  DEFAULT NULL            COMMENT '昵称',
  `email`       VARCHAR(64)  DEFAULT NULL            COMMENT '邮箱',
  `user_pic`    VARCHAR(512) DEFAULT NULL            COMMENT '头像URL',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `type`        TINYINT      DEFAULT 2               COMMENT '用户类型: 1=管理员, 2=读者',
  `state`       TINYINT      DEFAULT 1               COMMENT '借阅状态: 1=可借阅, 2=不可借阅',
  `msg`         VARCHAR(512) DEFAULT NULL            COMMENT '系统消息',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_type_state` (`type`, `state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 图书分类表
-- ============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`             INT         NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name`  VARCHAR(32) NOT NULL                COMMENT '分类名称',
  `category_alias` VARCHAR(32) NOT NULL                COMMENT '分类别名(英文标识)',
  `create_user`    INT         DEFAULT NULL            COMMENT '创建人ID',
  `create_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- ============================================
-- 3. 图书表
-- ============================================
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
  `id`          INT           NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `title`       VARCHAR(64)   NOT NULL                COMMENT '书名',
  `content`     TEXT          DEFAULT NULL            COMMENT '图书简介',
  `cover_img`   VARCHAR(512)  DEFAULT NULL            COMMENT '封面图片URL',
  `state`       VARCHAR(16)   DEFAULT '可借阅'        COMMENT '状态: 可借阅/已借出/已发布/草稿',
  `category_id` INT           DEFAULT NULL            COMMENT '分类ID',
  `create_user` INT           DEFAULT NULL            COMMENT '创建人ID',
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `location`    VARCHAR(32)   DEFAULT NULL            COMMENT '馆藏位置(如 A-01-03)',
  `bookNum`     INT           DEFAULT NULL            COMMENT '图书编号(6位)',
  `publisher`   VARCHAR(64)   DEFAULT NULL            COMMENT '出版社',
  `ISBN`        VARCHAR(32)   DEFAULT NULL            COMMENT 'ISBN号',
  `isbnPhoto`   VARCHAR(512)  DEFAULT NULL            COMMENT 'ISBN照片URL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bookNum` (`bookNum`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_state` (`state`),
  KEY `idx_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- ============================================
-- 4. 借阅记录表
-- ============================================
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record` (
  `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `borrowDate`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '借阅时间',
  `deadline`    DATETIME DEFAULT NULL              COMMENT '应归还时间',
  `borrowState` TINYINT  DEFAULT 1                 COMMENT '借阅状态: 1=借阅中, 2=已归还, 3=逾期',
  `userId`      INT      NOT NULL                  COMMENT '借阅人ID',
  `bookNum`     INT      NOT NULL                  COMMENT '图书编号',
  PRIMARY KEY (`id`),
  KEY `idx_userId` (`userId`),
  KEY `idx_bookNum` (`bookNum`),
  KEY `idx_borrowState` (`borrowState`),
  KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- ============================================
-- 5. 续借申请表
-- ============================================
DROP TABLE IF EXISTS `applyrecord`;
CREATE TABLE `applyrecord` (
  `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `borrowRecordId`  INT          NOT NULL                COMMENT '借阅记录ID',
  `bookNum`         INT          NOT NULL                COMMENT '图书编号',
  `userId`          INT          NOT NULL                COMMENT '申请人ID',
  `dayNum`          INT          NOT NULL                COMMENT '申请续借天数',
  `applyState`      TINYINT      DEFAULT 1               COMMENT '申请状态: 1=申请中, 2=已通过, 3=已拒绝',
  `msg`             VARCHAR(256) DEFAULT NULL            COMMENT '申请备注/审批消息',
  PRIMARY KEY (`id`),
  KEY `idx_borrowRecordId` (`borrowRecordId`),
  KEY `idx_userId` (`userId`),
  KEY `idx_applyState` (`applyState`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='续借申请表';

-- ============================================
-- 初始数据
-- ============================================

-- 默认账号（密码均为 MD5 哈希，对应明文见注释）
-- admin     / admin123  → MD5: 0192023a7bbd73250516f069df18b500
-- reader01  / 123456    → MD5: e10adc3949ba59abbe56e057f20f883e
-- reader02  / 123456    → MD5: e10adc3949ba59abbe56e057f20f883e
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `type`, `state`) VALUES
('admin',    '0192023a7bbd73250516f069df18b500', '系统管理员', 'admin@booksys.com', 1, 1),
('reader01', 'e10adc3949ba59abbe56e057f20f883e', '读者张三',   'zhangsan@test.com', 2, 1),
('reader02', 'e10adc3949ba59abbe56e057f20f883e', '读者李四',   'lisi@test.com',     2, 1);

-- 默认分类
INSERT INTO `category` (`category_name`, `category_alias`) VALUES
('文学',   'wenxue'),
('历史',   'lishi'),
('科学',   'kexue'),
('计算机', 'computer'),
('哲学',   'zhexue'),
('经济',   'jingji'),
('艺术',   'yishu'),
('其他',   'other');

-- 示例图书
INSERT INTO `article` (`title`, `content`, `cover_img`, `state`, `category_id`, `create_user`, `location`, `bookNum`, `publisher`, `ISBN`) VALUES
('百年孤独',   '《百年孤独》是哥伦比亚作家加西亚·马尔克斯的代表作...', '', '可借阅', 1, 1, 'A-01-01', 100001, '南海出版公司', '9787544253994'),
('活着',       '《活着》是中国作家余华的代表作...',               '', '可借阅', 1, 1, 'A-01-02', 100002, '作家出版社',   '9787506365437'),
('三体',       '《三体》是中国作家刘慈欣创作的科幻小说...',       '', '可借阅', 3, 1, 'A-03-01', 100003, '重庆出版社',   '9787536692930'),
('人类简史',   '《人类简史》是以色列历史学家尤瓦尔·赫拉利...',   '', '可借阅', 2, 1, 'A-02-01', 100004, '中信出版社',   '9787508647357'),
('设计模式',   '《设计模式》是GoF的经典著作...',                 '', '可借阅', 4, 1, 'A-04-01', 100005, '机械工业出版社', '9787111161905');
