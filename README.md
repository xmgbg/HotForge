# HotForge · 热练

<div align="center">

**集录播跟练、直播课程、训练计划与健康管理于一体的在线健身平台**

从课程发布、审核与排期，到高并发抢课、体测追踪和会员服务，构建完整的数字化健身业务闭环。

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.17-1E90FF?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat-square&logo=redis&logoColor=white)
![Status](https://img.shields.io/badge/Status-Active%20Development-FF6B35?style=flat-square)

</div>

---

## 项目简介

HotForge（热练）是一个采用前后端分离架构的综合在线健身平台。产品形态参考 Keep，用户既可以随时学习官方或教练发布的录播课程，也可以参加开放直播和限额小班课，并通过训练计划、学习记录和体测曲线持续管理健身过程。

系统采用“账号 + 能力 + 权益”的身份模型：

- **普通账号（USER）**：浏览与学习课程、参加直播、创建训练计划、管理训练及体测记录
- **教练能力**：普通账号认证通过后可发布录播课程和直播课程，不需要创建另一套账号
- **VIP 权益**：独立于教练身份，提供会员专享录播和直播，不包含提前抢课
- **管理员（ADMIN）**：审核教练、课程与排期，维护平台运行秩序

项目当前以学习和工程实践为目标，重点探索 Spring Boot 后端分层设计、JWT 无状态认证、课程学习业务，以及限额小班课高并发抢课场景下的数据一致性。

## 核心能力规划

| 业务领域 | 能力说明 |
| --- | --- |
| 用户体系 | 手机号与密码登录、模拟验证码注册/找回、JWT 身份认证、教练认证 |
| 录播跟练 | 官方及教练录播、章节学习、倍速播放、续看与完成记录 |
| 直播课程 | 开放直播直接参与，限额小班课按规则抢课 |
| 课程发现 | 名称搜索，以及部位、难度、器械、时长筛选、收藏和人工精选 |
| 训练管理 | 平台或教练训练计划、用户自定义计划、训练记录 |
| 高并发抢课 | Redisson 用户级锁 + Redis Lua 原子扣减 + MySQL 唯一约束 |
| 体测追踪 | 体重、体脂和围度记录及变化曲线 |
| VIP 服务 | 模拟购买、订单、续费、有效期和会员专享课程，不做提前抢课 |
| 平台服务 | 站内消息、教练及课程审核、健身 AI 问答 |
| 平台治理 | 教练认证、内容审核、操作日志与统一异常响应 |

## 技术架构

```mermaid
flowchart LR
    A[Vue 3 / Web Client] -->|REST API| B[Spring Security + JWT]
    B --> C[Controller]
    C --> D[Service]
    D --> E[MyBatis-Plus]
    E --> F[(MySQL 8)]
    D --> G[Redisson]
    G --> H[(Redis 7)]
```

### 后端技术栈

- Java 17
- Spring Boot 4.1.0
- Spring Security + JWT
- MyBatis-Plus Boot 4 Starter 3.5.17
- MySQL 8.0
- Redis 7.x + Redisson 4.6.1
- Maven

### 前端规划

- Vue 3
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios

## 高并发抢课设计

HotForge 将并发治理集中在限额小班课的抢课链路中；开放直播无需抢课：

```text
用户发起抢课
    │
    ├─ Redisson 锁：限制同一用户对同一场次的并发请求
    │
    ├─ Redis Lua：原子完成名单判重、库存校验与库存扣减
    │
    ├─ MySQL：同步写入预约记录，唯一索引提供最终判重保障
    │
    └─ 写入失败：执行反向 Lua 脚本，回补名单与库存
```

该方案让约满和重复请求尽可能在 Redis 层快速结束，减少无效数据库写入，同时通过唯一约束、失败回滚与定时对账控制数据一致性风险。

## 数据模型

项目当前已有 8 张基础业务表。课程章节、学习进度、训练计划、收藏、站内消息和 AI 会话等数据结构将按项目设计文档在后续里程碑补充：

| 数据表 | 用途 |
| --- | --- |
| `user` | 基础角色、教练资格、VIP 权益、账号状态及令牌版本 |
| `trainer_application` | 教练认证申请与审核记录 |
| `course` | 课程内容及审核状态 |
| `course_session` | 直播课程排期与容量 |
| `booking` | 用户抢课及预约状态 |
| `fitness_record` | 用户体测与健康数据 |
| `vip_order` | VIP 购买记录与有效期 |
| `operation_log` | 平台关键操作审计日志 |

完整数据库定义位于项目根目录的 [`.sql`](./.sql)。

## 项目结构

```text
HotForge/
├─ src/main/java/org/example/hotforge/
│  ├─ common/         # 统一响应、状态码与异常体系
│  ├─ config/         # Security、JWT、Redis 等配置
│  ├─ controller/     # REST API 接口层
│  ├─ dto/            # 请求与响应数据模型
│  ├─ entity/         # MyBatis-Plus 数据实体
│  ├─ mapper/         # 数据访问层
│  ├─ service/        # 核心业务逻辑
│  └─ util/           # 通用工具类
├─ src/main/resources/
│  └─ application.yml
├─ src/test/          # 自动化测试
├─ .sql               # 数据库结构与初始化数据
└─ pom.xml
```

## 当前进度

### 已完成

- [x] Spring Boot 项目骨架与基础依赖
- [x] 8 张核心业务表及初始化 SQL
- [x] Entity 与 Mapper 数据访问层
- [x] 统一响应结果与业务状态码
- [x] 全局异常处理体系
- [x] JWT 生成、解析与认证过滤器
- [x] Spring Security 无状态认证配置
- [x] 用户注册、登录及个人信息接口
- [x] 普通账号、教练资格与 VIP 权益相互独立的身份模型
- [x] Maven Wrapper、Spring Boot 4 依赖兼容与 H2 测试环境
- [x] 应用上下文、用户 Service、JWT 与用户接口自动化测试（共 15 项）

### 正在推进

- [ ] 模拟验证码、密码找回及令牌版本失效机制
- [ ] 教练认证申请、管理员审核与资格状态管理
- [ ] 课程章节、发布、查询与审核
- [ ] 直播课程排期管理
- [ ] Redis 库存预热与高并发抢课
- [ ] 预约取消、库存回补与定时对账
- [ ] 体测记录管理
- [ ] VIP 模拟购买、续费与会员有效期管理
- [ ] 教练、VIP 与管理员接口权限控制

### 后续规划

- [ ] Vue 3 前端基础框架与用户端页面
- [ ] 教练工作台与管理后台
- [ ] 前后端联调及业务测试
- [ ] Docker Compose 环境编排
- [ ] 生产部署与性能压测

## 本地启动

### 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x

### 初始化步骤

1. 克隆项目并进入目录。
2. 创建名为 `hotforge` 的 MySQL 数据库。
3. 执行根目录 `.sql` 初始化表结构与基础数据。
4. 启动 MySQL 与 Redis。
5. 设置本地环境变量，敏感信息不要写入仓库：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="<你的本地数据库密码>"
$env:JWT_SECRET="<使用 openssl rand -base64 32 生成的密钥>"
```

6. 运行自动化测试（测试使用 H2，不连接本地 MySQL 和 Redis）：

```powershell
.\mvnw.cmd test
```

7. 运行项目：

```powershell
.\mvnw.cmd spring-boot:run
```

<div align="center">

**Forge Your Strength, Feel the Heat.**

持续打磨中 · HotForge

</div>
