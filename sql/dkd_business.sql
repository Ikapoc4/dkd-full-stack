-- ============================================================
-- 帝可得 dkd · 业务表建表脚本
-- ------------------------------------------------------------
-- 用法：
--   mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS dkd DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
--   mysql -u root -p123456 dkd < sql/dkd_business.sql
--
-- 说明：
--   1. 这 6 张表是业务表，RuoYi 自带的 19+11 张系统表在 ry_20231130.sql / quartz.sql 里
--   2. 表名前缀 tb_，配合 dkd-generator 的 tablePrefix 配置使用
--   3. 本文件末尾有演示数据，方便第一次跑通验证；正式数据请自行清理
-- ============================================================

-- ------------------------------------------------------------
-- 1. 商品表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_sku;
CREATE TABLE tb_sku (
  sku_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品Id',
  sku_name     VARCHAR(64)   NOT NULL                COMMENT '商品名称',
  brand_name   VARCHAR(64)   DEFAULT NULL            COMMENT '品牌',
  unit         VARCHAR(16)   DEFAULT NULL            COMMENT '单位(瓶/袋/盒)',
  price        DECIMAL(10,2) DEFAULT NULL            COMMENT '售价',
  sku_image    VARCHAR(255)  DEFAULT NULL            COMMENT '商品图片',
  status       INT           NOT NULL DEFAULT 0      COMMENT '状态(0正常 1下架)',
  create_by    VARCHAR(64)   DEFAULT '',
  create_time  DATETIME      DEFAULT NULL,
  update_by    VARCHAR(64)   DEFAULT '',
  update_time  DATETIME      DEFAULT NULL,
  remark       VARCHAR(500)  DEFAULT NULL,
  PRIMARY KEY (sku_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ------------------------------------------------------------
-- 2. 点位表（机器装在哪儿）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_node;
CREATE TABLE tb_node (
  node_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '点位Id',
  node_name    VARCHAR(64)   NOT NULL                COMMENT '点位名称',
  address      VARCHAR(255)  DEFAULT NULL            COMMENT '详细地址',
  region_id    BIGINT        DEFAULT NULL            COMMENT '区域Id(分片用)',
  region_name  VARCHAR(64)   DEFAULT NULL            COMMENT '区域名称',
  partner_id   BIGINT        DEFAULT NULL            COMMENT '合作商Id',
  status       INT           NOT NULL DEFAULT 1      COMMENT '状态(0停用 1启用)',
  create_by    VARCHAR(64)   DEFAULT '',
  create_time  DATETIME      DEFAULT NULL,
  update_by    VARCHAR(64)   DEFAULT '',
  update_time  DATETIME      DEFAULT NULL,
  remark       VARCHAR(500)  DEFAULT NULL,
  PRIMARY KEY (node_id),
  KEY idx_region (region_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='点位表';

-- ------------------------------------------------------------
-- 3. 设备表（售货机）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_vm;
CREATE TABLE tb_vm (
  vm_id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备Id',
  vm_code          VARCHAR(32)  NOT NULL                COMMENT '设备编号(如VM001)',
  node_id          BIGINT       DEFAULT NULL            COMMENT '所属点位Id',
  status           INT          NOT NULL DEFAULT 0      COMMENT '状态(0未投放 1运营 3撤机)',
  last_supply_time DATETIME     DEFAULT NULL            COMMENT '上次补货时间',
  create_by        VARCHAR(64)  DEFAULT '',
  create_time      DATETIME     DEFAULT NULL,
  update_by        VARCHAR(64)  DEFAULT '',
  update_time      DATETIME     DEFAULT NULL,
  remark           VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (vm_id),
  UNIQUE KEY uk_vm_code (vm_code),
  KEY idx_node (node_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ------------------------------------------------------------
-- 4. 货道表（★ 定时任务扫的就是这张表）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_channel;
CREATE TABLE tb_channel (
  channel_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '货道Id',
  vm_id        BIGINT       NOT NULL                COMMENT '设备Id',
  sku_id       BIGINT       DEFAULT NULL            COMMENT '商品Id(空货道为NULL)',
  channel_code VARCHAR(20)  NOT NULL                COMMENT '货道编号(如1-2 表示1行2列)',
  max_capacity INT          NOT NULL DEFAULT 0      COMMENT '最大容量',
  stock        INT          NOT NULL DEFAULT 0      COMMENT '当前库存',
  threshold    INT          NOT NULL DEFAULT 0      COMMENT '补货阈值(★核心字段)',
  task_id      BIGINT       DEFAULT NULL            COMMENT '当前未完成补货工单Id(幂等用)',
  create_by    VARCHAR(64)  DEFAULT '',
  create_time  DATETIME     DEFAULT NULL,
  update_by    VARCHAR(64)  DEFAULT '',
  update_time  DATETIME     DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (channel_id),
  UNIQUE KEY uk_vm_channel (vm_id, channel_code),
  KEY idx_vm (vm_id),
  KEY idx_task (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='货道表';

-- ------------------------------------------------------------
-- 5. 工单表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_task;
CREATE TABLE tb_task (
  task_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工单Id',
  task_type      INT          NOT NULL                COMMENT '工单类型(1投放 2补货 3维修 4撤机)',
  task_status    INT          NOT NULL DEFAULT 1      COMMENT '工单状态(1创建 2进行 3取消 4完成)',
  node_id        BIGINT       DEFAULT NULL            COMMENT '点位Id',
  vm_id          BIGINT       DEFAULT NULL            COMMENT '设备Id',
  create_user_id BIGINT       DEFAULT NULL            COMMENT '创建人Id(0=系统自动创建)',
  assign_user_id BIGINT       DEFAULT NULL            COMMENT '指派给的运维员Id',
  task_desc      VARCHAR(500) DEFAULT NULL            COMMENT '工单描述',
  create_by      VARCHAR(64)  DEFAULT '',
  create_time    DATETIME     DEFAULT NULL,
  update_by      VARCHAR(64)  DEFAULT '',
  update_time    DATETIME     DEFAULT NULL,
  remark         VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (task_id),
  KEY idx_status_type (task_status, task_type),
  KEY idx_assign (assign_user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- ------------------------------------------------------------
-- 6. 工单明细表
--    注意：这张表没有 create_by / update_by 等公共列
-- ------------------------------------------------------------
DROP TABLE IF EXISTS tb_task_detail;
CREATE TABLE tb_task_detail (
  detail_id  BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细Id',
  task_id    BIGINT NOT NULL                COMMENT '工单Id',
  channel_id BIGINT NOT NULL                COMMENT '货道Id',
  expect_num INT    DEFAULT 0               COMMENT '应补数量',
  real_num   INT    DEFAULT 0               COMMENT '实补数量',
  PRIMARY KEY (detail_id),
  KEY idx_task (task_id),
  KEY idx_channel (channel_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='工单明细表';

-- ============================================================
-- 演示数据（第一次跑通用；正式使用请自行清理）
-- ============================================================

INSERT INTO tb_sku (sku_name, brand_name, unit, price, status, create_by, create_time)
VALUES ('可口可乐330ml', '可口可乐', '瓶', 3.00, 0, 'admin', sysdate()),
       ('农夫山泉550ml', '农夫山泉', '瓶', 2.00, 0, 'admin', sysdate());

INSERT INTO tb_node (node_name, address, region_id, region_name, status, create_by, create_time)
VALUES ('万达广场1号门', '北京市朝阳区建国路93号', 1, '朝阳区', 1, 'admin', sysdate());

INSERT INTO tb_vm (vm_code, node_id, status, create_by, create_time)
VALUES ('VM001', 1, 1, 'admin', sysdate());

-- 1-2 号货道故意低于阈值，用来验证自动补货
INSERT INTO tb_channel (vm_id, sku_id, channel_code, max_capacity, stock, threshold, create_by, create_time)
VALUES (1, 1, '1-1', 10, 8, 3, 'admin', sysdate()),
       (1, 1, '1-2', 10, 2, 3, 'admin', sysdate()),
       (1, 2, '1-3', 10, 9, 3, 'admin', sysdate());
