# dkd-manage 模块设计文档 · 手敲版

> 目标：把这个空模块填成一个能跑的售货机业务后端，并且跑通「库存低于阈值 → 自动建补货工单」这一条核心链路。
> 全程自己敲。每一步都有**验证**——不通过就别往下走。
>
> 配套阅读：`项目笔记.md`（背景与决策）、`从零跑起来.md`（环境）、`README.md`（业务介绍）

---

## 第 0 章 · 先搞清楚在写什么

**这个模块是「服务器后台」的业务代码，不是售货机的代码。**

```
① [售货机硬件]   ← 不在本仓库，不是我们写的
       │ 上报「3号货道还剩1瓶」
       ▼
② [后台服务器 = 本仓库]  ← 我们写的就是这一层的 dkd-manage
       ├── dkd-manage（业务）
       └── MySQL
       ▲
       │ 运营员在网页上点「新增点位」「补货完成」
       │
③ [管理端网页]   ← RuoYi 送的
```

**manage 只处理两类请求：**

| 类型 | 触发者 | 干什么 |
|---|---|---|
| **人点按钮** | 运营员/管理员 | 维护 点位/设备/货道/商品 —— 本质是 CRUD |
| **时间到点** | 定时器 | 扫货道库存 → 低于阈值就建补货工单 |

**整个项目的核心其实就是一个循环：**

```
tb_channel.stock 低了  →  自动建 tb_task  +  tb_task_detail  →  运维员补完货  →  stock 改回去
```

> 库存这个数字真实项目里由售货机上报，**我们这里手动 UPDATE 模拟即可**。
> 所以演示只靠三步：`UPDATE stock = 0` → 等定时任务 → 看 `tb_task` 多出一条。

---

## 第 1 章 · 数据模型（6 张表）

```
tb_node 点位 ──1:N──▶ tb_vm 设备 ──1:N──▶ tb_channel 货道 ──N:1──▶ tb_sku 商品
                                    │                                   
                                    └──────────▶ tb_task 工单 ──1:N──▶ tb_task_detail 工单明细
```

| 表 | 一句话 | 谁在用 |
|---|---|---|
| `tb_node` | 机器摆在哪儿（万达广场1号门） | 运营员维护 |
| `tb_sku` | 卖什么（可口可乐 330ml 3元） | 运营员维护 |
| `tb_vm` | 哪台机器（VM001，装在哪个点位） | 运营员维护 |
| `tb_channel` | 机器的格子（1行2列放可乐，容量10，还剩0，阈值3） | 运营员维护 + **定时任务扫描** |
| `tb_task` | 工单（给VM001补货，派给张三） | 系统自动建 + 人手动建 |
| `tb_task_detail` | 工单明细（这次要补：3-1号货道补5瓶） | 跟工单一起建 |

### 1.1 公共字段

每个 domain 都继承 `BaseEntity`（[BaseEntity.java](dkd-common/src/main/java/com/dkd/common/core/domain/BaseEntity.java)），所以每张表都要有这 5 列：

| Java 字段 | 列名 | 类型 |
|---|---|---|
| `createBy` | `create_by` | varchar(64) |
| `createTime` | `create_time` | datetime |
| `updateBy` | `update_by` | varchar(64) |
| `updateTime` | `update_time` | datetime |
| `remark` | `remark` | varchar(500) |

> `BaseEntity` 里还有 `searchValue`（`@JsonIgnore`）和 `params`（Map，装自定义查询参数）——**这两个不是数据库列**，别建。

### 1.2 完整 DDL

新建 `sql/dkd_business.sql`，把下面全抄进去。

> **为什么状态字段用 `INT` 不用 `TINYINT`**：`DkdContants` 里定义的是 `Long`（`public static final Long TASK_TYPE_SUPPLY = 2l;`），用 `INT` 能让 MyBatis 干干净净映射成 `Long`，省掉类型转换的坑。

```sql
-- ============================================================
-- 帝可得 · 业务表
-- ============================================================

-- 1. 商品
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

-- 2. 点位（机器装在哪儿）
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

-- 3. 设备（售货机）
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

-- 4. 货道（★ 定时任务扫的就是这张表）
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

-- 5. 工单
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

-- 6. 工单明细
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

-- 7. 演示数据（手动模拟"售货机上报库存"）
INSERT INTO tb_sku (sku_name, brand_name, unit, price, status, create_by, create_time)
VALUES ('可口可乐330ml', '可口可乐', '瓶', 3.00, 0, 'admin', sysdate()),
       ('农夫山泉550ml', '农夫山泉', '瓶', 2.00, 0, 'admin', sysdate());

INSERT INTO tb_node (node_name, address, region_id, region_name, status, create_by, create_time)
VALUES ('万达广场1号门', '北京市朝阳区建国路93号', 1, '朝阳区', 1, 'admin', sysdate());

INSERT INTO tb_vm (vm_code, node_id, status, create_by, create_time)
VALUES ('VM001', 1, 1, 'admin', sysdate());

INSERT INTO tb_channel (vm_id, sku_id, channel_code, max_capacity, stock, threshold, create_by, create_time)
VALUES (1, 1, '1-1', 10, 8, 3, 'admin', sysdate()),
       (1, 1, '1-2', 10, 2, 3, 'admin', sysdate()),   -- ★ 故意低于阈值，用来验证调度
       (1, 2, '1-3', 10, 9, 3, 'admin', sysdate());
```

**验证**：

```bash
mysql -u root -p123456 dkd -e "SHOW TABLES;"
mysql -u root -p123456 dkd -e "SELECT channel_id,channel_code,stock,threshold FROM tb_channel;"
```

期望看到 6 张 `tb_` 表，货道 1-2 的 `stock=2 < threshold=3`。

---

## 第 2 章 · 目录结构（要建的每一个文件夹）

**记住：全部在 `dkd-manage` 里，Controller 也不放 dkd-admin。**

```
dkd-manage/
├── pom.xml                                          ← 已存在，基本不用改
└── src/main/
    ├── java/com/dkd/manage/
    │   ├── controller/        ← HTTP 入口（Web 层）
    │   │   ├── NodeController.java
    │   │   ├── SkuController.java
    │   │   ├── VmController.java
    │   │   ├── ChannelController.java
    │   │   └── TaskController.java
    │   ├── domain/            ← 实体（对应表）
    │   │   ├── Node.java  Sku.java  Vm.java  Channel.java  Task.java  TaskDetail.java
    │   │   └── vo/
    │   │       └── ChannelStockVO.java      ← 缺货货道的查询结果（带商品名/设备编号）
    │   ├── mapper/            ← 数据层接口（没有实现类！）
    │   │   ├── NodeMapper.java  SkuMapper.java  VmMapper.java
    │   │   ├── ChannelMapper.java  TaskMapper.java  TaskDetailMapper.java
    │   └── service/
    │       ├── INodeService.java  ...
    │       ├── impl/
    │       │   ├── NodeServiceImpl.java  ...
    │       │   └── SupplyTaskJob.java       ← ★ 自动补货调度的入口
    │       └── ...
    └── resources/mapper/manage/      ← SQL XML 必须放这儿
        ├── NodeMapper.xml  SkuMapper.xml  VmMapper.xml
        ├── ChannelMapper.xml  TaskMapper.xml  TaskDetailMapper.xml
```

### 2.1 为什么这样放就能被扫到（一行配置都不用改）

| 配置 | 位置 | 通配符 |
|---|---|---|
| `@MapperScan("com.dkd.**.mapper")` | [ApplicationConfig.java:19](dkd-framework/src/main/java/com/dkd/framework/config/ApplicationConfig.java#L19) | 覆盖 `com.dkd.manage.mapper` ✅ |
| `typeAliasesPackage: com.dkd.**.domain` | [application.yml:103](dkd-admin/src/main/resources/application.yml#L103) | 覆盖 `com.dkd.manage.domain` ✅ |
| `mapperLocations: classpath*:mapper/**/*Mapper.xml` | [application.yml:105](dkd-admin/src/main/resources/application.yml#L105) | 覆盖 `resources/mapper/manage/` ✅ |
| `@SpringBootApplication` 在 `com.dkd` | [DkdApplication.java](dkd-admin/src/main/java/com/dkd/DkdApplication.java) | 覆盖 `com.dkd.manage.controller` ✅ |

### 2.2 依赖方向（背下来）

```
dkd-admin ──▶ dkd-framework ──▶ dkd-system ──▶ dkd-common
                  ▲
            dkd-manage
```

- `dkd-manage` 能用 **common + system + framework 的一切**（`BaseController`、`AjaxResult`、`SecurityUtils`、`@Log`、`SysUser`、`PageHelper`…）
- **绝对不要在 dkd-manage 里 import `com.dkd.web.*`**（那是 admin，依赖方向反了，编译不过）

---

## 第 3 章 · 施工顺序（6 个阶段）

> **每一阶段结束都必须验证通过，再进下一阶段。**

### 阶段 0 · 准备（10 分钟）

| # | 做什么 | 为什么 |
|---|---|---|
| 0.1 | 把 `sql/dkd_business.sql` 导进 `dkd` 库 | 没表什么都写不了 |
| 0.2 | 改 [generator.yml](dkd-generator/src/main/resources/generator.yml)：`packageName: com.dkd.manage`、`tablePrefix: tb_` | 不改的话代码生成器会把业务代码吐进 `dkd-system`，跟 RuoYi 混在一起 |
| 0.3 | `dkd-manage/pom.xml` 里那两行 `maven.compiler.source/target = 11` 删掉 | 干净。**不会炸**（根 pom 的插件配置 `<source>${java.version}</source>` 优先级更高），但留着容易误导 |

**验证**：`mvn clean package -DskipTests` 出现 `BUILD SUCCESS`。

---

### 阶段 1 · 手敲「点位」这一条链路（★ 最重要的一阶段）

**目的：把 RuoYi 的分层套路摸熟。这一条链路通了，后面 5 张表全是复制粘贴。**

要敲 **6 个文件**，顺序就是从下往上（表 → domain → mapper → xml → service → controller）：

| # | 文件 | 内容 | 关键点 |
|---|---|---|---|
| 1.1 | `domain/Node.java` | 继承 `BaseEntity`，字段对应 `tb_node` 的列 | 加 `@Excel(name=...)` 就能导出 Excel |
| 1.2 | `mapper/NodeMapper.java` | **纯接口**，方法名 = XML 的 id | 加 `@Mapper` 注解或不加都行（有 `@MapperScan`） |
| 1.3 | `resources/mapper/manage/NodeMapper.xml` | `namespace` 指向 1.2 的全限定名 | `<resultMap>` 做下划线→驼峰映射 |
| 1.4 | `service/INodeService.java` | 接口 | 跟 Mapper 方法一一对应 |
| 1.5 | `service/impl/NodeServiceImpl.java` | 加 `@Service`，注入 `NodeMapper` | **业务规则写这里**，不写 Controller |
| 1.6 | `controller/NodeController.java` | 继承 `BaseController`，加 `@RestController` | 照抄 [SysPostController.java](dkd-admin/src/main/java/com/dkd/web/controller/system/SysPostController.java) |

**直接抄的模板**（6 个文件一一对应）：

| 你要写的 | 抄这个 |
|---|---|
| `Node.java` | [SysPost.java](dkd-system/src/main/java/com/dkd/system/domain/SysPost.java) |
| `NodeMapper.java` | [SysPostMapper.java](dkd-system/src/main/java/com/dkd/system/mapper/SysPostMapper.java) |
| `NodeMapper.xml` | [SysPostMapper.xml](dkd-system/src/main/resources/mapper/system/SysPostMapper.xml) |
| `INodeService.java` | `dkd-system/.../service/ISysPostService.java` |
| `NodeServiceImpl.java` | [SysPostServiceImpl.java](dkd-system/src/main/java/com/dkd/system/service/impl/SysPostServiceImpl.java) |
| `NodeController.java` | [SysPostController.java](dkd-admin/src/main/java/com/dkd/web/controller/system/SysPostController.java) |

**URL 规划**（统一前缀 `/manage`）：

```
GET    /manage/node/list        → list(Node node)      startPage() + getDataTable()
GET    /manage/node/{nodeId}    → getInfo(@PathVariable Long nodeId)
POST   /manage/node             → add(@Validated @RequestBody Node node)
PUT    /manage/node             → edit(@Validated @RequestBody Node node)
DELETE /manage/node/{nodeIds}   → remove(@PathVariable Long[] nodeIds)
```

**权限标识**：`manage:node:list` / `manage:node:query` / `manage:node:add` / `manage:node:edit` / `manage:node:remove`

> **注意**：`admin` 用户在 RuoYi 里拥有 `*:*:*`，所以 `@PreAuthorize` 对你**不会拦**。
> 这些权限码真正生效是在你新建了普通角色之后。**前期不用插菜单，用 admin 直接调接口即可。**

**验证**（三选一）：

1. 浏览器打开 `http://localhost:8080/manage/node/list`（带 token）→ 返回 `{code:200, rows:[...], total:1}`
2. Swagger：`http://localhost:8080/swagger-ui/index.html`
3. 日志里看 SQL 有没有打出来

**这一步过了，你就"通"了。**后面所有表都是这个套路。

---

### 阶段 2 · 剩下 3 张基础表（用代码生成器，别手敲）

`tb_sku` / `tb_vm` / `tb_channel` 三张表，**用代码生成器生成**：

1. 启动项目 → 登录 → 左侧「系统工具」→「代码生成」
2. 导入 `tb_sku`、`tb_vm`、`tb_channel`
3. 每张表点「编辑」→ 把**生成包路径**改成 `com.dkd.manage`、**生成模块名**填 `manage`
4. 点「生成代码」→ 下载 zip → 把 `main/java/...` 和 `main/resources/mapper/manage/...` 解压进 `dkd-manage`
5. 生成的 Controller/Service 检查一遍（生成器偶尔会漏点东西），把 `/system/xxx` 的 `@RequestMapping` 改成 `/manage/xxx`

**验证**：`GET /manage/sku/list`、`/manage/vm/list`、`/manage/channel/list` 都能返回数据。

> 生成器生成的代码是"标准 CRUD"，**没有业务逻辑**。业务逻辑是你后面自己加的。

---

### 阶段 3 · 工单表 + 手动建单

`tb_task` / `tb_task_detail` 也生成出来，然后**手写一个"手动建补货工单"的接口**：

```
POST /manage/task/supply/{vmId}
```

逻辑（写在 `TaskServiceImpl` 里）：

```
1. 查这台设备有哪些货道 stock <= threshold
2. 没有 → 抛 ServiceException("没有需要补货的货道")
3. 有 → 插入一条 tb_task
        task_type   = DkdContants.TASK_TYPE_SUPPLY   (2)
        task_status = DkdContants.TASK_STATUS_CREATE (1)
        create_user_id = getUserId()   ← 手动建的，记当前人
4. 每个缺货货道插一条 tb_task_detail
        expect_num = max_capacity - stock
5. 把 tb_channel.task_id 更新成新工单的 id
```

**验证**：手工调一次接口，`SELECT * FROM tb_task` 有数据，`tb_task_detail` 条数对得上。

> 这一步是为了先**手工跑通建单逻辑**。阶段 4 只是把它交给定时器自动调用——**同一段代码**。

---

### 阶段 4 · 自动补货调度（★ 核心）

**把阶段 3 的逻辑抽成一个方法，然后让定时器调它。**

最小实现（先用 Spring 自带的，最简单）：

```java
// service/impl/SupplyTaskJob.java
@Component
public class SupplyTaskJob {

    @Autowired
    private ITaskService taskService;

    /** 每 5 分钟扫一次 */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void autoSupply() {
        taskService.autoCreateSupplyTask();
    }
}
```

**别忘了在启动类加 `@EnableScheduling`** —— 不加 `@Scheduled` 就是一段死代码，不报错也不执行（最容易踩的坑）。

**验证**：

```sql
-- 1. 把某条货道改成缺货
UPDATE tb_channel SET stock = 0 WHERE channel_id = 1;
```

等 5 分钟（或者临时把 cron 改成 `0/10 * * * * ?` 每 10 秒），然后：

```sql
SELECT task_id, task_type, task_status, vm_id, create_user_id FROM tb_task ORDER BY task_id DESC;
SELECT * FROM tb_task_detail WHERE task_id = <上面新的task_id>;
SELECT channel_id, stock, task_id FROM tb_channel WHERE channel_id = 1;
```

期望：`tb_task` 多出一条 `task_type=2` `create_user_id=0`（系统建的）的工单，明细里有货道 1，`tb_channel.task_id` 指向它。

**到这里，"自动补货工单调度"就跑通了。**

---

### 阶段 5 · 做深：分片 + 锁 + 幂等（★ 面试要讲的就是这一段）

阶段 4 的版本**单机跑没问题，多实例就出问题**。这一阶段专门解决它。

#### 5.1 问题是什么（先把问题说清楚）

| 问题 | 现象 | 原因 |
|---|---|---|
| **重复建单** | 同一个货道冒出两张补货工单 | 两个实例同时扫到它，都判断"没有未完成工单"，都插了一条 |
| **任务重试重复** | 定时任务失败重试，又建一遍 | 上一次其实已经建成功了 |
| **单机太慢** | 2000 个点位扫一遍要很久 | 只有一个实例在扫 |

#### 5.2 三个解法

**① 分片（解决慢）**

```java
// 每台机器启动时知道自己是谁
@Value("${dkd.shard.index:0}")    private int shardIndex;
@Value("${dkd.shard.total:1}")    private int shardTotal;

// SQL 里按点位分片
// select * from tb_channel c
//   join tb_vm v on v.vm_id = c.vm_id
//  where v.node_id % #{shardTotal} = #{shardIndex}
```

启动两台实例，分别配 `dkd.shard.index=0` 和 `=1`，各扫一半点位。

**② 分布式锁（解决同一货道被并发处理）**

```java
RLock lock = redissonClient.getLock("dkd:lock:channel:" + channelId);
try {
    if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
        // 建单逻辑
    }
} finally {
    if (lock.isHeldByCurrentThread()) lock.unlock();
}
```

> 需要给 `dkd-manage/pom.xml` 加 `redisson-spring-boot-starter` 依赖。
> **注意**：锁只能保证"同一时刻只有一个在跑"，**不能防重试**——重试时锁早就释放了。

**③ 抢占式幂等（★ 最漂亮的一招，解决重复建单）**

利用 `tb_channel.task_id`，用一条 UPDATE 做"抢占"：

```sql
UPDATE tb_channel
   SET task_id = #{newTaskId}
 WHERE channel_id = #{channelId}
   AND task_id IS NULL          -- ★ 只有还没被占的才能抢到
```

```java
int rows = channelMapper.occupyChannel(channelId, newTaskId);
if (rows == 0) {
    return;   // 别人已经建过单了，直接跳过
}
// rows == 1 才继续建 tb_task / tb_task_detail
```

**为什么这样就不会重复**：`task_id IS NULL` 这个条件和 UPDATE 是**同一个原子操作**，数据库的行锁保证了只有一个人能把它从 NULL 改成功。

> **对比一下三个阶段**：
> - 阶段 4：`if (查不到未完成工单) { 建单 }` —— **先查后写，中间有窗口，会重复** ❌
> - 阶段 5②：加锁 —— 窗口变小但还是先查后写，**重试时照样重复** ⚠️
> - 阶段 5③：`UPDATE ... WHERE task_id IS NULL` —— **一条 SQL 原子抢占，重试也不会重复** ✅

#### 5.3 怎么测出数字（面试要的"实测"）

```
1. 写个脚本，一次性把 200 个货道的 stock 改成 0
2. 启动两个实例（端口 8081 / 8082，shard.index 分别 0 / 1）
3. 等两边都跑完一轮
4. 检查：
   SELECT channel_id, COUNT(*) FROM tb_task_detail GROUP BY channel_id HAVING COUNT(*) > 1;
   → 期望：0 行（没有重复）
5. 把 ③ 的 `AND task_id IS NULL` 去掉，重跑一次
   → 期望：出现重复行
```

> **这一步跑出来的 0 行 vs N 行，就是你面试时能说的数字。** 不是编的。

#### 5.4 关于定时任务选型

| 方案 | 分片 | 页面配 cron | 集群 | 难度 |
|---|---|---|---|---|
| Spring `@Scheduled` | ❌ 自己实现 | ❌ | ❌ | ★ |
| RuoYi 自带 Quartz | ❌ 自己实现 | ✅ | 要配 JDBC JobStore | ★★ |
| **XXL-Job** | ✅ **内置分片广播** | ✅ | ✅ | ★★★ |

**建议路线**：阶段 4 用 `@Scheduled` 跑通逻辑 → 阶段 5 加锁和幂等 → **最后再换 XXL-Job**（因为简历上写的就是它，而且它的「分片广播」正好替你干了 5.2① 的活）。

---

### 阶段 6 · AI 故障诊断（可选，最后做）

挂在工单上：运维员用大白话描述现象 → RAG 检索**历史工单 + 故障手册** → 输出排查步骤。

```
dkd 后端（Java）──HTTP──▶ ai-service（Python/FastAPI）
                              └── RAG + BGE 精排 + Milvus
```

**先把 Java 侧做完再做这个。** Java 侧没跑通，AI 侧没有数据可查。

---

## 第 4 章 · 施工总清单（打勾用）

### 阶段 0
- [ ] 建库 + 导 `ry_20231130.sql`、`quartz.sql`
- [ ] 导 `sql/dkd_business.sql`
- [ ] 改 `generator.yml` 的 `packageName` / `tablePrefix`
- [ ] 删 `dkd-manage/pom.xml` 里的 compiler 11
- [ ] `mvn clean package -DskipTests` 成功

### 阶段 1（手敲）
- [ ] `domain/Node.java`
- [ ] `mapper/NodeMapper.java`
- [ ] `resources/mapper/manage/NodeMapper.xml`
- [ ] `service/INodeService.java`
- [ ] `service/impl/NodeServiceImpl.java`
- [ ] `controller/NodeController.java`
- [ ] `GET /manage/node/list` 返回数据

### 阶段 2（生成）
- [ ] `tb_sku` → Sku 四件套
- [ ] `tb_vm` → Vm 四件套
- [ ] `tb_channel` → Channel 四件套
- [ ] 三张表的 `/manage/xxx/list` 都能返回

### 阶段 3
- [ ] `tb_task`、`tb_task_detail` 生成
- [ ] 手写 `POST /manage/task/supply/{vmId}`
- [ ] 验证：能建出工单 + 明细 + 回填 `channel.task_id`

### 阶段 4
- [ ] `SupplyTaskJob` + `@EnableScheduling`
- [ ] `UPDATE tb_channel SET stock = 0` → 等 → 看工单自动出现

### 阶段 5
- [ ] 分片：`shardIndex` / `shardTotal`
- [ ] Redisson 分布式锁
- [ ] 抢占式幂等：`UPDATE ... WHERE task_id IS NULL`
- [ ] 双实例实测：重复行 = 0
- [ ] 换 XXL-Job

### 阶段 6
- [ ] ai-service（Python/FastAPI）
- [ ] RAG 检索历史工单 + 故障手册
- [ ] 挂在工单详情页

---

## 第 5 章 · 易错点（踩了再回来看）

| 现象 | 原因 | 处理 |
|---|---|---|
| 启动报 `Invalid bound statement (not found)` | Mapper 方法名 ≠ XML 的 `id`，或 XML 放错位置 | XML 必须在 `src/main/resources/mapper/**/`，`namespace` 写全限定名 |
| `@Scheduled` 不执行 | 启动类忘了 `@EnableScheduling` | 加注解。**不报错、不执行，最难查** |
| 分页失效 | `startPage()` 后面先写了别的查询 | `startPage()` 必须**紧挨着**目标查询 |
| 生成器把代码吐进 `dkd-system` | `generator.yml` 的 `packageName` 没改 | 改成 `com.dkd.manage` |
| MyBatis 报类型转换错 | `task_type` 建成了 `TINYINT`，Java 用 `Long` | 状态字段统一用 `INT` |
| 重复建工单 | 先查后写，中间有窗口 | 用 `UPDATE ... WHERE task_id IS NULL` 抢占 |
| `@PreAuthorize` 不生效 | 用 admin 登录（有 `*:*:*`） | 正常。新建普通角色后才看得出效果 |
| 想显示菜单但网页上没有 | `sys_menu` 里没插数据 | 见附录 SQL |
| 中文乱码 | 建库/建表字符集不对 | 统一 `utf8mb4` |

---

## 附录 A · 常量（已存在，直接用）

[DkdContants.java](dkd-common/src/main/java/com/dkd/common/constant/DkdContants.java)：

```java
TASK_TYPE_DEPLOY = 1L     // 投放工单
TASK_TYPE_SUPPLY = 2L     // 补货工单  ★ 你主要用这个
TASK_TYPE_REPAIR = 3L     // 维修工单
TASK_TYPE_REVOKE = 4L     // 撤机工单

TASK_STATUS_CREATE   = 1L // 创建(待处理)  ★
TASK_STATUS_PROGRESS = 2L // 进行
TASK_STATUS_CANCEL   = 3L // 取消
TASK_STATUS_FINISH   = 4L // 完成

VM_STATUS_NODEPLOY = 0L   // 未投放
VM_STATUS_RUNNING  = 1L   // 运营  ★
VM_STATUS_REVOKE   = 3L   // 撤机

ROLE_CODE_BUSINESS = "1002"  // 运营员
ROLE_CODE_OPERATOR = "1003"  // 维修员（派单派给他）
```

**不要改这个文件**（它在 `dkd-common`）。货道相关的新常量放 `dkd-manage` 里，比如 `com.dkd.manage.constant.ChannelConstants`。

## 附录 B · 让左侧菜单出现（后期再说）

前期用 admin 调接口就行，**不用插菜单**。要做页面时：

```sql
-- 1. 先建一个一级目录「运营管理」
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('运营管理', 0, 10, 'manage', NULL, 1, 0, 'M', '0', '0', '', 'build', 'admin', sysdate(), '运营管理目录');

-- 记下刚插入的 menu_id，假设是 2000

-- 2. 挂子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache,
                      menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('点位管理', @parentId, 1, 'node', 'manage/node/index', 1, 0, 'C', '0', '0',
        'manage:node:list', 'tree', 'admin', sysdate(), '点位菜单'),
       ('设备管理', @parentId, 2, 'vm',   'manage/vm/index',   1, 0, 'C', '0', '0',
        'manage:vm:list',   'server', 'admin', sysdate(), '设备菜单'),
       ('货道管理', @parentId, 3, 'channel', 'manage/channel/index', 1, 0, 'C', '0', '0',
        'manage:channel:list', 'grid', 'admin', sysdate(), '货道菜单'),
       ('工单管理', @parentId, 4, 'task', 'manage/task/index', 1, 0, 'C', '0', '0',
        'manage:task:list', 'job', 'admin', sysdate(), '工单菜单');
```

> 插完要给角色授权（系统管理 → 角色 → 编辑 → 勾上菜单），admin 不用授权。
> **前端页面要自己写**（`dkd-vue/src/views/manage/node/index.vue`），可以用阶段 2 生成的 Vue 代码。

## 附录 C · 一条链路的分工（背下来）

| 层 | 文件 | 职责 | 绝对不做 |
|---|---|---|---|
| Controller | `NodeController` | 翻译 HTTP、权限、参数校验、包装返回 | ❌ 写 SQL、❌ 开事务、❌ 注入 Mapper |
| Service | `INodeService` + `NodeServiceImpl` | **业务规则**、事务 | ❌ 认识 HttpServletRequest |
| Mapper | `NodeMapper`（接口） | 声明要哪些查询 | ❌ 有实现类 |
| XML | `NodeMapper.xml` | 真正的 SQL | — |
| Domain | `Node` | 数据长什么样（继承 `BaseEntity`） | — |

**依赖方向**：`Controller → Service → Mapper → XML → MySQL`，**单向，不许回头**。
