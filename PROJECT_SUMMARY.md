# BookSys 项目概览

> 生成日期：2026-05-04 | 分支：main | 状态：开发中（有未提交更改）

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

---

## 三、项目结构

```
src/
├── main/java/com/tem/booksys/
│   ├── BookSysApplication.java        # 主入口（@EnableScheduling）
│   ├── anno/State.java                # 自定义校验注解
│   ├── config/WebMvcConfig.java       # MVC 配置（拦截器注册）
│   ├── controller/
│   │   ├── UserController.java        # /user/*  用户注册/登录/管理
│   │   ├── BookController.java        # /article/*  图书 CRUD + 条码/二维码
│   │   ├── BorrowController.java      # /borrow/*  借阅/归还/续借
│   │   ├── CategoryController.java    # /category/*  分类管理
│   │   └── FileUploadController.java  # /load  文件上传至 OSS
│   ├── entiy/                         # 实体类（⚠ 包名有 typo）
│   │   ├── User.java                  # 用户（MD5 密码）
│   │   ├── Article.java               # 图书/文章
│   │   ├── Category.java              # 分类
│   │   ├── BorrowRecord.java          # 借阅记录
│   │   ├── ApplyRecord.java           # 续借申请
│   │   ├── Result.java                # 统一响应 {code, message, data}
│   │   └── PageBean.java              # 分页包装 {total, items}
│   ├── exception/
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── interceptors/
│   │   └── LoginInterceptor.java      # JWT + Redis 登录拦截器
│   ├── mapper/                        # MyBatis Mapper 接口
│   ├── service/                       # 业务层接口 + Impl 实现
│   ├── utils/                         # 工具类（11 个）
│   └── validation/
│       └── StateValidation.java       # @State 校验器实现
├── main/resources/
│   ├── application.yml                # 核心配置
│   └── mybatis/mapper/               # MyBatis XML（5 个文件）
└── test/                              # 测试代码（大部分已注释）
```

---

## 四、数据库表（5 张，由 JPA 自动建表）

| 表名 | 对应实体 | 说明 |
|------|----------|------|
| `user` | User | 用户（type: 1=管理员/2=读者, state: 1=可借阅/2=不可借阅） |
| `article` | Article | 图书（含 ISBN、ISBN 图片、位置、出版社等） |
| `category` | Category | 图书分类 |
| `borrow_record` | BorrowRecord | 借阅记录（含借阅/逾期状态） |
| `applyrecord` | ApplyRecord | 续借申请（需管理员审批） |

---

## 五、核心业务流程

1. **用户注册**：邮箱验证码（Redis 存储，6 位数字） → 密码 MD5 加密 → 入库
2. **用户登录**：验证账号密码 → 生成 JWT → 存入 Redis（1 小时 TTL）
3. **图书管理**：增删改查 + 分类筛选 + AI 生成简介 + 条码识别 + 二维码生成
4. **借阅流程**：校验用户状态 → 检查逾期 → 限制最多 6 本 → synchronzied 并发控制 → 写入借阅记录
5. **归还流程**：更新借阅状态 → 恢复图书可借数量
6. **续借流程**：读者申请 → 管理员审批（通过/拒绝）
7. **逾期处理**：定时任务每日凌晨 1:10:30 检查，标记逾期 → 发送邮件提醒
8. **文件上传**：MultipartFile → UUID 重命名 → 上传阿里云 OSS → 返回 URL

---

## 六、外部集成

| 服务 | 用途 | 配置位置 |
|------|------|----------|
| 阿里云 OSS | 图片/文件存储 | `AliOssUtil.java`（Bucket: ryan4real） |
| 百度 ERNIE | AI 图书简介生成 | `ChineseGPT.java` |
| 163 邮箱 | 验证码发送 + 逾期催还 | `application.yml` + `TaskTimeUtil.java` |
| Redis | Token 存储 + 验证码缓存 | `application.yml`（localhost:6379） |
| Python 脚本 | 条码识别 | `JavaCallPython.java` 调用外部 Python 进程 |

---

## 七、安全问题（已知）

- [ ] **阿里云 OSS AK/SK** 硬编码在 `AliOssUtil.java`
- [ ] **邮箱密码** 明文写在 `application.yml`
- [ ] **百度 ERNIE API Key/Secret** 硬编码在 `ChineseGPT.java`
- [ ] **JWT 签名密钥** 使用固定弱密钥 `itheima`
- [ ] **密码加密** 使用 MD5（不安全，应升级为 BCrypt）
- [ ] **数据库密码** 明文写在 `application.yml`
- [ ] JPA `ddl-auto: create` —— **每次启动都会重建所有表**，生产环境会丢失数据

---

## 八、未提交更改（当前分支 main）

4 个文件被修改：
- `pom.xml` — 依赖调整
- `BookSysApplication.java` — 启动类改动
- `User.java` — 实体字段变更
- `application.yml` — 配置修改（34 行变更）

---

## 九、技术债与改进建议

1. **包名 typo**：`entiy/` → 应为 `entity/`
2. **测试覆盖**：几乎为零，所有测试类都被注释掉
3. **硬编码路径**：`BarcodeReader.java` 和 `JavaCallPython.java` 使用绝对路径 `C:/Users/Rain/Desktop/...`
4. **敏感信息**：全部凭据应迁移到环境变量或配置中心
5. **前端缺失**：目前仅有 `static/index.html` 占位页，功能全为后端 REST API
6. **并发控制过于粗糙**：`BorrowController` 使用 `synchronized` 代码块，高并发下性能不佳
7. **Druid 监控**：已引入但未配置监控页面
8. **日志**：缺少统一的日志框架配置（仅打印 SQL）
