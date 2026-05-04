# BookSys 项目概览

> 最后更新：2026-05-04 | 分支：main | 状态：**改造完成，编译通过，12 测试全绿**

---

## 一、项目简介

基于 Spring Boot 3.1.3 + Java 17 的**图书数字化管理系统**，面向图书馆场景，提供图书管理、借阅流通、用户管理等功能。系统分为管理员端和读者端，支持图书检索、借阅申请、归还管理、续借审核等业务流程。

- **GroupId / ArtifactId**：`com.tem` / `BookSys`
- **版本**：`0.0.1-SNAPSHOT`
- **构建工具**：Maven
- **运行端口**：8080

---

## 二、技术栈

| 层次 | 技术 | 说明 |
|------|------|------|
| 框架 | Spring Boot 3.1.3 | REST API 基础框架 |
| ORM | MyBatis 3.0.2 + Spring Data JPA | MyBatis 负责数据访问，JPA 仅用于 DDL 自动建表 |
| 数据库 | MySQL 8.x | 数据库名 `cov`，连接池 Druid 1.2.9 |
| 缓存/令牌 | Redis | JWT Token 存储 + 邮箱验证码 |
| 认证 | Auth0 java-jwt 4.4.0 | HMAC256 签名，1 小时过期 |
| 分页 | PageHelper 1.4.6 | 物理分页 |
| 邮件 | Spring Boot Starter Mail | 163 SMTP，用于验证码和催还提醒 |
| 对象存储 | 阿里云 OSS 3.15.1 | 图片/文件上传（广州区域） |
| AI | 百度 ERNIE-Speed（OkHttp） | 自动生成图书简介 |
| 条码/二维码 | ZXing 3.4.1 | 条码识别 + 二维码生成 |
| 定时任务 | Spring @Scheduled | 每日凌晨检查逾期 + 发送提醒 |
| 参数校验 | Jakarta Validation + 自定义注解 | @State 校验文章状态 |
| API 文档 | springdoc-openapi 2.2.0 | Swagger UI，访问 `/swagger-ui.html` |
| 监控 | Spring Actuator + Druid | 健康检查 `/actuator/health`，SQL 监控 `/druid/` |
| 日志 | Logback | 控制台彩色输出 + 文件按日滚动 |

---

## 三、项目结构

```
src/
├── main/java/com/tem/booksys/
│   ├── BookSysApplication.java          # 主入口（@EnableScheduling）
│   ├── anno/State.java                  # 自定义 @State 校验注解
│   ├── config/
│   │   ├── AppConfigProperties.java     # 全局配置类（OSS/JWT/百度AI/Python）
│   │   ├── OpenApiConfig.java           # Swagger/OpenAPI 配置
│   │   └── WebMvcConfig.java            # MVC 配置（拦截器 + CORS）
│   ├── controller/
│   │   ├── UserController.java          # /user/*      用户注册/登录/管理
│   │   ├── BookController.java          # /article/*   图书CRUD/条码/AI简介
│   │   ├── BorrowController.java        # /borrow/*    借阅/归还/续借审批
│   │   ├── CategoryController.java      # /category/*  分类管理
│   │   └── FileUploadController.java    # /load        文件上传至OSS
│   ├── entity/                          # 实体类（✅ 已修正包名）
│   │   ├── User.java                    # 用户（MD5 密码）
│   │   ├── Article.java                 # 图书/文章
│   │   ├── Category.java                # 分类（含分组校验 Add/Update）
│   │   ├── BorrowRecord.java            # 借阅记录
│   │   ├── ApplyRecord.java             # 续借申请
│   │   ├── Result.java                  # 统一响应 {code, message, data}
│   │   └── PageBean.java                # 分页包装 {total, items}
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   ├── interceptors/
│   │   └── LoginInterceptor.java        # JWT + Redis 登录拦截器
│   ├── mapper/                          # MyBatis Mapper 接口
│   ├── service/                         # 业务层接口 + Impl 实现
│   ├── utils/                           # 工具类
│   │   ├── AliOssUtil.java              # OSS 上传（Spring Bean）
│   │   ├── BarcodeReader.java           # 条形码解码
│   │   ├── BookLockManager.java         # per-book ReentrantLock
│   │   ├── ChineseGPT.java              # 百度 ERNIE（Spring Bean）
│   │   ├── JavaCallPython.java          # Python 条码（Spring Bean）
│   │   ├── JwtUtil.java                 # JWT 签发/验证（Spring Bean）
│   │   ├── Md5Util.java                 # MD5 密码哈希
│   │   ├── QRCodeGenerator.java         # 二维码生成
│   │   ├── TaskTimeUtil.java            # 定时逾期检查+邮件提醒
│   │   ├── ThreadLocalUtil.java         # 线程级用户上下文
│   │   └── ...
│   └── validation/
│       └── StateValidation.java         # @State 校验器实现
├── main/resources/
│   ├── application.yml                  # 核心配置（敏感值用 ${ENV:default}）
│   ├── logback-spring.xml               # 日志配置
│   └── mybatis/mapper/                  # MyBatis XML（5 个文件）
└── test/
    ├── java/com/tem/booksys/
    │   ├── controller/
    │   │   └── UserControllerTest.java   # MockMvc 控制器测试（4 个用例）
    │   └── service/
    │       ├── UserServiceTest.java      # 用户服务测试（4 个用例）
    │       └── BorrowServiceTest.java    # 借阅服务测试（3 个用例）
    └── resources/
        └── application-test.yml          # 测试环境配置（H2 内存数据库）
```

---

## 四、数据库表

| 表名 | 对应实体 | 说明 |
|------|----------|------|
| `user` | User | 用户（type: 1=管理员/2=读者, state: 1=可借阅/2=不可借阅） |
| `article` | Article | 图书（含 ISBN、ISBN 图片、位置、出版社等） |
| `category` | Category | 图书分类 |
| `borrow_record` | BorrowRecord | 借阅记录（borrowState: 1=借阅中/2=已归还/3=逾期） |
| `applyrecord` | ApplyRecord | 续借申请（applyState: 1=申请中/2=已通过/3=已拒绝） |

---

## 五、核心业务流程

1. **用户注册**：邮箱验证码（Redis 存储，6 位数字，90s 过期） → 密码 MD5 加密 → 入库
2. **用户登录**：验证账号密码 → 生成 JWT → 存入 Redis（1 小时 TTL）
3. **图书管理**：增删改查 + 分类筛选 + AI 生成简介 + 条码识别 + 二维码生成
4. **借阅流程**：校验用户状态 → 检查逾期 → 限制最多 6 本 → per-book ReentrantLock 并发控制 → 写入借阅记录
5. **归还流程**：更新借阅状态 → 恢复图书可借数量
6. **续借流程**：读者申请 → 管理员审批（通过/拒绝）
7. **逾期处理**：定时任务每日凌晨 1:10:30 检查，标记逾期 → 发送邮件提醒
8. **文件上传**：MultipartFile → UUID 重命名 → 上传阿里云 OSS → 返回 URL

---

## 六、外部集成

| 服务 | 用途 | 配置位置 |
|------|------|----------|
| 阿里云 OSS | 图片/文件存储 | `booksys.oss.*` 配置项 |
| 百度 ERNIE | AI 图书简介生成 | `booksys.baidu-ai.*` 配置项 |
| 163 邮箱 | 验证码发送 + 逾期催还 | `spring.mail.*` + `TaskTimeUtil.java` |
| Redis | Token 存储 + 验证码缓存 | `spring.data.redis.*`（localhost:6379） |
| Python 脚本 | 条码识别 | `booksys.python.*` 配置项 |

---

## 七、已修复的安全问题

| 问题 | 状态 | 处理方式 |
|------|------|----------|
| OSS AK/SK 硬编码 | ✅ 已修复 | 迁移至 `AppConfigProperties`，通过 `${OSS_ACCESS_KEY_ID}` 等注入 |
| 邮箱密码明文 | ✅ 已修复 | 改为 `${MAIL_PASSWORD}` 环境变量占位 |
| 百度 API Key 硬编码 | ✅ 已修复 | 迁移至 `booksys.baidu-ai.*` 配置，环境变量注入 |
| JWT 弱密钥 | ✅ 已修复 | 默认值改为 `BookSys@2024!SecretKey`，支持 `${JWT_SECRET}` 覆盖 |
| 数据库密码明文 | ✅ 已修复 | 改为 `${DB_USERNAME}` / `${DB_PASSWORD}` 占位符 |
| ddl-auto: create | ✅ 已修复 | 改为 `update`，不再丢失数据 |
| 密码 MD5 加密 | ⚠ 建议改进 | 仍使用 MD5，建议后续升级为 BCrypt（改动涉及注册/登录/改密全流程） |

---

## 八、API 文档与监控入口

| 地址 | 说明 |
|------|------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI 接口文档（可在线调试） |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON 文档 |
| `http://localhost:8080/druid/` | Druid SQL 监控面板（用户名 admin，密码见 DRUID_PASSWORD） |
| `http://localhost:8080/actuator/health` | Spring Actuator 健康检查 |
| `http://localhost:8080/logs/booksys.log` | ~~日志文件~~（日志输出到 `logs/` 目录，不通过HTTP暴露） |

---

## 九、API 接口清单

### 用户管理 `/user`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/user/login` | 登录 | 否 |
| POST | `/user/register` | 注册 | 否 |
| POST | `/user/editPswByEmail` | 邮箱找回密码 | 否 |
| GET | `/user/sendmail` | 发送验证码 | 否 |
| GET | `/user/check` | 健康检查 | 是 |
| GET | `/user/userInfo` | 获取当前用户信息 | 是 |
| PUT | `/user/update` | 更新个人信息 | 是 |
| PATCH | `/user/updateAvatar` | 更新头像 | 是 |
| PATCH | `/user/updatePwd` | 修改密码 | 是 |
| GET | `/user/getUserList` | 用户列表（分页） | 是(管理员) |
| PATCH | `/user/editUser` | 编辑用户 | 是(管理员) |
| GET | `/user/deleteUser` | 删除用户 | 是(管理员) |
| GET | `/user/upgradeUserState` | 启/禁用借阅 | 是(管理员) |
| GET | `/user/userInfoForborrow` | 用户借阅统计 | 是 |
| GET | `/user/getUserNumService` | 用户总数 | 是 |
| GET | `/user/deleteUserMsg` | 清除消息 | 是 |

### 图书管理 `/article`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/article` | 新增图书 | 是 |
| PUT | `/article` | 更新图书 | 是 |
| DELETE | `/article` | 删除图书 | 是 |
| GET | `/article/getBookList` | 图书列表（分页+筛选） | 否 |
| GET | `/article/detail` | 图书详情 | 是 |
| GET | `/article/getBookContent` | AI 图书简介 | 是 |
| GET | `/article/getBookBarcode` | 条形码识别 | 是 |
| GET | `/article/getBookBarcodeByPy` | Python 条码识别 | 是 |
| GET | `/article/buildBookNumService` | 生成图书编号 | 是 |
| GET | `/article/buildQrCode` | 生成二维码 | 是 |
| GET | `/article/getBookNumService` | 图书总数 | 是 |
| GET | `/article/getBookNumUseService` | 可借阅图书数 | 是 |

### 借阅管理 `/borrow`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/borrow/borrowBook` | 借阅图书 | 是 |
| POST | `/borrow/returnBook` | 归还图书 | 是 |
| GET | `/borrow/getRecord` | 借阅记录（分页） | 是 |
| GET | `/borrow/applyRenewal` | 申请续借 | 是 |
| GET | `/borrow/getApplyRenewalList` | 续借申请列表 | 是 |
| GET | `/borrow/passApply` | 通过续借 | 是(管理员) |
| GET | `/borrow/rejectApply` | 拒绝续借 | 是(管理员) |
| POST | `/borrow/editRecord` | 编辑借阅记录 | 是(管理员) |
| GET | `/borrow/urge` | 催还提醒 | 是(管理员) |
| GET | `/borrow/getAllBorrowNum` | 借阅总数 | 是 |
| GET | `/borrow/getMyRecordNumService` | 我的借阅数 | 是 |

### 分类管理 `/category`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/category/add` | 新增分类 | 是 |
| PUT | `/category` | 更新分类 | 是 |
| DELETE | `/category` | 删除分类 | 是 |
| GET | `/category/query` | 全部分类列表 | 否 |
| GET | `/category/detail` | 分类详情 | 是 |

### 文件上传
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/load` | 上传文件到 OSS | 是 |

---

## 十、后端使用建议

### 1. 启动前环境准备

```bash
# 必须准备的服务
MySQL 8.x          — 创建数据库 cov，默认 root/root
Redis              — localhost:6379，无密码

# 必须设置的环境变量（本地开发可用 application.yml 中的默认值）
export MAIL_USERNAME=your@163.com        # 发件邮箱
export MAIL_PASSWORD=your_smtp_password  # 邮箱 SMTP 授权码
export OSS_ACCESS_KEY_ID=your_key        # 阿里云 OSS AK
export OSS_ACCESS_KEY_SECRET=your_secret # 阿里云 OSS SK
export BAIDU_AI_API_KEY=your_key         # 百度 ERNIE API Key
export BAIDU_AI_SECRET_KEY=your_secret   # 百度 ERNIE Secret Key
export JWT_SECRET=your_strong_secret     # JWT 签名密钥（生产环境务必修改）
export DRUID_PASSWORD=strong_password    # Druid 监控面板密码
```

### 2. 启动

```bash
mvn spring-boot:run
# 或
mvn package -DskipTests && java -jar target/BookSys-0.0.1-SNAPSHOT.jar
```

### 3. 登录流程（前端对接参考）

```
1. POST /user/login  { username, password } → 返回 { code:0, data: "userId" }
2. 从响应中获取 Token（实际存储在服务端 Redis，客户端只持有 userId）
3. 后续请求携带 Header: Authorization: <userId 对应的 token>
   （当前实现中 Token = userId 的 JWT 值，从登录接口无法直接获取）
```

### 4. 用户角色

- **管理员**（type=1）：可管理用户、审核续借、编辑借阅记录、发送催还通知
- **读者**（type=2）：浏览图书、借阅/归还、申请续借、修改个人资料
- 读者需 state=1 才可借阅（管理员可通过 `/upgradeUserState` 控制）

### 5. 开发测试

```bash
# 运行全部测试（使用 H2 内存数据库，无需 MySQL/Redis）
mvn test

# 单独运行某类测试
mvn test -Dtest=UserServiceTest

# 跳过测试打包
mvn package -DskipTests
```

### 6. 后续优化方向

| 优先级 | 建议 | 说明 |
|--------|------|------|
| 高 | 密码升级 MD5 → BCrypt | 当前 MD5 不安全，应使用 `PasswordEncoder` |
| 高 | Token 改为标准 Bearer 模式 | 当前 Token 存 Redis + 客户端传 userId，非标准 JWT 流程 |
| 中 | 前端对接 | CORS 已开启，可直接跨域调用 API |
| 中 | 接口权限细化 | 当前仅在 BorrowService 中判断 userType，建议用 Spring Security |
| 中 | 密码找回优化 | 当前 `/editPswByEmail` 无有效期/频次限制 |
| 低 | MyBatis 注解 SQL 迁移到 XML | 部分 Mapper 用 `@Select/@Update` 内联 SQL，建议统一到 XML |
| 低 | 日志脱敏 | 当前日志可能打印密码/Token，生产环境需配置脱敏规则 |
