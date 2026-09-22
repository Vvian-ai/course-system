# 校园课程管理系统

[![CI](https://github.com/Vvian-ai/course-system/actions/workflows/ci.yml/badge.svg)](https://github.com/Vvian-ai/course-system/actions/workflows/ci.yml)

基于 Spring Boot 3 的课程管理系统，面向学生、教师、管理员三种角色，覆盖选课、退课、成绩录入与课程管理的完整流程。

启动后接口文档地址：`http://localhost:8080/doc.html`

## 功能概览

| 角色 | 功能 |
| --- | --- |
| 学生 | 浏览课程、选课（每人限 5 门，且受课程容量约束）、退课、查看已选课程与成绩 |
| 教师 | 查看本人授课课程、查看选课名单、录入成绩（仅限本人授课的课程） |
| 管理员 | 课程 / 学生 / 教师的增删、分配与取消授课教师、代学生选课 |

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言与框架 | Java 21、Spring Boot 3.2.5、Spring MVC、Spring Data JPA |
| 数据存储 | MySQL 8、Redis（Spring Cache） |
| 认证鉴权 | JWT（JJWT）+ 拦截器实现接口级权限控制 |
| 接口文档 | Knife4j（OpenAPI 3） |
| 其他 | Thymeleaf、AOP 日志切面、JSR-303 参数校验、BCrypt 密码加密 |
| 测试 | JUnit 5、Mockito |

## 项目结构

```
src/main/java/com/example/course_system/
├── aspect/          AOP 日志切面：记录 URL、耗时、参数，并对密码字段脱敏
├── common/          统一返回结构 Result
├── config/          Web 配置、Redis 与缓存配置、BCrypt Bean、接口文档配置
├── dto/             对外传输对象，与实体解耦
├── entity/          JPA 实体
├── exception/       业务异常
├── handler/         全局异常处理
├── interceptor/     JWT 拦截器：认证 + 角色与资源归属校验
├── repository/      Spring Data JPA 仓库
├── service/         业务逻辑
└── util/            JWT 工具与 DTO 转换
```

前端页面位于 `src/main/resources/templates/`，表结构定义在 `src/main/resources/schema.sql`。

## 快速开始

### 1. 环境要求

- JDK 21
- MySQL 8（本地 3306）
- Redis（本地 6379）

### 2. 配置

复制配置模板并填入自己的信息：

```bash
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
```

需要填写：数据库账号密码、管理员账号（密码是 BCrypt 哈希）、JWT 密钥（至少 32 字符）。

### 3. 启动

```bash
./mvnw spring-boot:run
```

首次启动时会自动执行 `schema.sql` 建表（若表不存在），并插入一组演示数据。

访问 `http://localhost:8080/login`，接口文档在 `http://localhost:8080/doc.html`。

### 4. 演示账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 学生 | 1001 | 123456 |
| 教师 | 2001 | 123456 |
| 管理员 | 配置文件中设置 | 配置文件中设置 |

### 如何生成 BCrypt 哈希

管理员的密码在配置文件中以 BCrypt 哈希存储。可以借助项目依赖生成：

```java
new BCryptPasswordEncoder().encode("你的密码");
```

注意 BCrypt 每次生成的哈希都不同（带随机盐），这是正常的，任意一个都能用来校验。

## 设计要点

下面几处是开发过程中真实遇到并解决的问题，记录在这里供后续参考。

### 1. 权限校验必须在服务端

最初"学生只能查看自己的选课和成绩"只写在前端界面上——后端拦截器只校验了角色，而获取学号的参数是路径变量（`/api/enrollments/student/{id}`），拦截器用 `getParameter` 取不到，校验被直接跳过。结果是学生改一下 URL 就能看到别人的成绩。

修复后的做法：接口层和页面层都加上资源归属校验，学生的身份从 Token 中取出，不接受客户端传入。

### 2. 选课的唯一性与容量（并发超卖）

这一处的问题是一层层挖出来的，过程比结论更有意思。

**第一层：重复选课。**
「先查询是否已选，再插入」在并发下会失效——两个请求可以同时通过检查。
解决办法是在选课记录表建立 `(student_id, course_id)` 唯一索引，并在服务层加事务边界，
冲突时抛出可读的业务提示。

**第二层：超额选课。**
唯一索引只能防止同一学生重复选课，防不了容量超限。容量判断同样是「先查人数、再插入」，
两个不同的学生可以同时读到相同的人数、同时通过校验。

**第三层：加了锁还不够。**
给课程行加上悲观写锁（等价于 `SELECT ... FOR UPDATE`）后，同一门课的请求被串行化了，
但在 200 并发、请求同时到达的条件下**仍然超卖 9 人**。
原因是 MySQL 默认的 **REPEATABLE-READ** 隔离级别下，事务内的普通 `SELECT`
读取的是事务开始时的快照——锁保证了写入顺序，却保护不到快照读，
排队等待的请求用的仍是过期的人数。

**最终方案**：容量校验改用带 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 的查询，
并把该事务的隔离级别显式设为 `READ_COMMITTED`，让每次查询都读取最新的已提交数据。

**实测对比**（JMeter，200 并发，课程容量 50）：

| 并发保护 | Ramp-up 1 秒 | Ramp-up 0 秒 |
| --- | --- | --- |
| 无保护 | 51 人（超出 1） | 52 人（超出 2） |
| 悲观锁 | 50 人 | 59 人（超出 9） |
| 悲观锁 + READ_COMMITTED | — | **50 人** |

注意第二行与第三行的差别：同样加了锁，宽松条件下通过、严苛条件下失败。
**只在宽松条件下验证，很容易得出错误的结论。**

完整的五轮压测过程、排查思路与数据见 [`docs/压测记录.md`](docs/压测记录.md)，
复现用的数据准备脚本见 [`docs/压测数据准备.sql`](docs/压测数据准备.sql)。

**可进一步优化**：改用「条件更新」
（`UPDATE course SET enrolled_count = enrolled_count + 1 WHERE course_id = ? AND enrolled_count < capacity`），
用一条原子语句同时完成检查与扣减，比悲观锁少一次查询、锁持有时间更短。

### 3. 会话管理

JWT 最初只存放在 localStorage，退出登录时前端清掉本地存储，但服务端并不知道，Token 在有效期内仍然可用。现在改为写入 `HttpOnly` + `SameSite=Lax` 的 Cookie，并新增 `POST /api/logout` 接口由服务端清除 Cookie。前端不再读写 Token。

### 4. 缓存一致性

课程列表使用 Redis 缓存并设置 TTL；所有会改变课程数据的操作（新增、删除、分配/取消教师）都通过 `@CacheEvict` 主动失效，避免读到脏数据。缓存值的序列化方式从 JDK 序列化改为 JSON，减少与实体类的耦合。

### 5. 「未录入」的表示方式

成绩最初用 `-1` 作为「未录入」的哨兵值，语义混淆且容易和真实分数混淆。现已改为 `NULL`，并统一了三个页面的显示逻辑。

### 6. 表结构管理

从 `ddl-auto=update`（由 Hibernate 自动改表）改为 `schema.sql` + `ddl-auto=validate`，让表结构的变更显式可控。

## 接口一览

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/login` | 登录，返回 Token 并写入 Cookie | 公开 |
| POST | `/api/logout` | 退出登录，清除 Cookie | 公开 |
| GET | `/api/courses` | 课程列表（含教师姓名与已选人数） | 所有角色 |
| GET | `/api/courses/{id}` | 课程详情 | 所有角色 |
| POST | `/api/courses` | 新增课程 | 管理员 |
| DELETE | `/api/courses/{id}` | 删除课程（级联删除选课记录） | 管理员 |
| PUT | `/api/courses/assign` | 分配授课教师 | 管理员 |
| PUT | `/api/courses/unassign` | 取消授课教师 | 管理员 |
| GET | `/api/students` | 学生列表 | 所有角色 |
| GET | `/api/students/{id}` | 学生详情 | 所有角色 |
| POST | `/api/students` | 新增学生 | 管理员 |
| DELETE | `/api/students/{id}` | 删除学生（级联删除选课记录） | 管理员 |
| GET | `/api/teachers` | 教师列表 | 所有角色 |
| GET | `/api/teachers/{id}` | 教师详情 | 所有角色 |
| POST | `/api/teachers` | 新增教师 | 管理员 |
| DELETE | `/api/teachers/{id}` | 删除教师（解除其授课关系） | 管理员 |
| GET | `/api/enrollments/student/{studentId}` | 查询某学生的选课记录 | 学生本人 / 教师 / 管理员 |
| GET | `/api/enrollments/byCourse` | 查询某课程的选课名单 | 教师 / 管理员 |
| POST | `/api/enrollments` | 选课 | 学生 / 管理员 |
| DELETE | `/api/enrollments` | 退课 | 学生 |
| PUT | `/api/enrollments/grade` | 录入成绩 | 教师（仅本人授课课程） |

## 测试

```bash
./mvnw test
```

共 16 个用例：15 个覆盖 `CourseService` 的核心业务（选课上限、重复选课、课程容量、退课校验、成绩范围、教师越权、教师分配），1 个验证 Spring 上下文能否正常加载。

## 后续计划

- [ ] 为选课接口补充分页与条件查询
- [ ] 增加成绩修改的审计日志（记录操作人、修改前后分值）
- [ ] 引入选课时间窗与成绩录入截止时间的状态控制
- [ ] 选课容量校验改为条件更新，进一步降低锁开销
- [ ] 容器化部署（Docker Compose 一键拉起 MySQL + Redis + 应用）

## 已知问题

- `controller` 包下的源文件位于 `src/main/java/controller/`，而包名为 `com.example.course_system.controller`，目录与包名不一致。功能不受影响，但结构上不够规范，后续会统一到标准目录。
