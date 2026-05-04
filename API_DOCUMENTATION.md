# BookSys 接口文档（前端开发用）

> 来源：`http://localhost:8080/v3/api-docs` | 在线调试：`http://localhost:8080/docs`
> 生成时间：2026-05-04

---

## 目录

1. [通用约定](#1-通用约定)
2. [用户管理 /user](#2-用户管理-user)
3. [图书管理 /article](#3-图书管理-article)
4. [借阅管理 /borrow](#4-借阅管理-borrow)
5. [分类管理 /category](#5-分类管理-category)
6. [文件上传 /load](#6-文件上传-load)
7. [数据结构定义](#7-数据结构定义schemas)

---

## 1. 通用约定

### 1.1 基础地址

```
Base URL: http://localhost:8080
```

### 1.2 认证方式

- 登录后服务端生成 JWT Token 存入 Redis（过期 1 小时）
- 登录接口返回 `data` 为 **用户 ID**（Integer）
- 后续请求需在 Header 中传入 Token：

```
Authorization: <token值>
```

- **无需认证**的接口（白名单）：登录、注册、发送验证码、邮箱重置密码、分类列表、图书列表

### 1.3 统一响应格式

所有接口返回 JSON，结构如下：

```json
{
  "code": 0,          // 0=成功, 1=失败
  "message": "操作成功", // 提示信息
  "data": null        // 响应数据，类型见各接口
}
```

### 1.4 分页请求/响应

**请求参数**（Query String）：
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码，从 1 开始 |
| pageSize | int | 是 | 每页条数 |

**响应 data 结构**：
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "total": 100,
    "items": [...]
  }
}
```

---

## 2. 用户管理 /user

### 2.1 登录

```
POST /user/login
认证: 不需要
```

**请求参数**（URL Query 或 Form）：
| 参数 | 类型 | 必填 | 规则 |
|------|------|------|------|
| username | string | 是 | 5-16位非空字符 |
| password | string | 是 | 5-16位非空字符 |

**成功响应**：
```json
{ "code": 0, "message": "操作成功", "data": "1" }
```
> `data` 是用户 ID（String 类型），前端需保存用于后续请求 Header

**失败响应**：
| code | message | 原因 |
|------|---------|------|
| 1 | 该用户不存在 | 用户名未注册 |
| 1 | 密码错误 | 密码不匹配 |

**调用示例**：
```js
fetch('http://localhost:8080/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: 'username=testuser&password=123456'
})
```

---

### 2.2 注册

```
POST /user/register
认证: 不需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 5-16位非空字符 |
| password | string | 是 | 5-16位非空字符 |
| mail | string | 是 | 邮箱地址 |
| code | string | 是 | 邮箱验证码（先调 2.4 发送） |
| type | int | 否 | 用户类型：1=管理员，2=读者（默认2） |

**成功**：`{ "code": 0, "message": "操作成功", "data": null }`

**失败**：
| message | 原因 |
|---------|------|
| 用户名已经占用了 | 用户名重复 |
| 验证码已经过期 | Redis 中验证码已过期（90s） |
| 验证码错误 | 验证码不匹配 |

---

### 2.3 发送邮箱验证码

```
GET /user/sendmail
认证: 不需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| mail | string | 是 | 接收验证码的邮箱 |

> 验证码为 6 位数字，有效期 **90 秒**，存入 Redis

**成功**：`{ "code": 0, "message": "操作成功", "data": null }`

---

### 2.4 邮箱重置密码

```
POST /user/editPswByEmail
认证: 不需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| mail | string | 是 | 注册邮箱 |
| code | string | 是 | 邮箱验证码 |
| password | string | 是 | 新密码 |

**失败**：
| message | 原因 |
|---------|------|
| 邮箱不匹配 | 邮箱与注册邮箱不一致 |
| 验证码已经过期 | 验证码超时 |
| 验证码错误 | 验证码不匹配 |

---

### 2.5 获取当前用户信息

```
GET /user/userInfo
认证: 需要
```

无参数，根据 Header 中的 Token 自动识别当前用户。

**成功**：
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@test.com",
    "userPic": "https://...",
    "createTime": "2026-01-01T00:00:00",
    "updateTime": "2026-01-01T00:00:00",
    "type": 2,
    "state": 1,
    "msg": "您有书籍即将逾期..."
  }
}
```

**字段说明**：
| 字段 | 类型 | 说明 |
|------|------|------|
| type | int | 1=管理员，2=读者 |
| state | int | 1=可借阅，2=不可借阅 |
| msg | string | 系统消息（逾期提醒等） |
| userPic | string | 头像 URL |

---

### 2.6 更新个人信息

```
PUT /user/update
认证: 需要
Content-Type: application/json
```

**请求体**（JSON）：
```json
{
  "id": 1,
  "username": "testuser",
  "nickname": "新昵称",
  "email": "new@test.com"
}
```
> `id` 和 `nickname`、`email` 为必填

---

### 2.7 更新头像

```
PATCH /user/updateAvatar
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| avatarUrl | string | 是 | 头像 URL（需合法 URL 格式） |

---

### 2.8 修改密码

```
PATCH /user/updatePwd
认证: 需要
Content-Type: application/json
```

**请求头**：`Authorization: <token>`（修改成功后 Token 会失效）

**请求体**：
```json
{
  "oldpassword": "旧密码",
  "password": "新密码",
  "repassword": "确认新密码"
}
```

> 规则：新密码 5-16 位非空字符，两次输入需一致

---

### 2.9 用户列表（管理员）

```
GET /user/getUserList
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |
| username | string | 否 | 按用户名筛选 |
| state | int | 否 | 按状态筛选 |

**响应 data**：`PageBean<User>`

---

### 2.10 编辑用户（管理员）

```
PATCH /user/editUser
认证: 需要（管理员）
Content-Type: application/json
```

```json
{
  "id": "1",
  "nickname": "新昵称",
  "password": "新密码"
}
```
> nickname 和 password 可只传其一

---

### 2.11 启/禁用用户借阅（管理员）

```
GET /user/upgradeUserState
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 用户 ID |
| state | int | 是 | 1=可借阅，2=不可借阅 |

---

### 2.12 删除用户（管理员）

```
GET /user/deleteUser
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 |
|------|------|------|
| id | string | 是 |

---

### 2.13 查询用户借阅统计

```
GET /user/userInfoForborrow
认证: 需要
```

| 参数 | 类型 | 必填 |
|------|------|------|
| userId | string | 是 |

**响应 data**：
```json
{
  "borrowNum": 3,
  "overdueNum": 1
}
```

---

### 2.14 获取用户总数

```
GET /user/getUserNumService
认证: 需要
```

响应 data：`Integer`

---

### 2.15 清除用户消息

```
GET /user/deleteUserMsg
认证: 需要
```

清除当前登录用户的系统消息（msg 字段）。

---

### 2.16 健康检查

```
GET /user/check
认证: 需要
```

---

## 3. 图书管理 /article

### 3.1 新增图书

```
POST /article
认证: 需要
Content-Type: application/json
```

```json
{
  "title": "书名（必填，1-10位）",
  "content": "图书简介内容（必填）",
  "coverImg": "封面图片URL（必填）",
  "state": "已发布",
  "categoryId": 1,
  "location": "A-01-03",
  "bookNum": 100001,
  "publisher": "出版社名称",
  "isbn": "9787222204331",
  "isbnPhoto": "ISBN照片URL"
}
```

**字段约束**：
| 字段 | 约束 | 必填 |
|------|------|------|
| title | 1-10位非空字符 | 是 |
| content | 任意文本 | 是 |
| coverImg | 图片 URL | 是 |
| state | `已发布` 或 `草稿` | 否 |
| categoryId | int | 是 |

---

### 3.2 更新图书

```
PUT /article
认证: 需要
Content-Type: application/json
```

请求体同 3.1（根据 bookNum 定位图书）。

---

### 3.3 删除图书

```
DELETE /article
认证: 需要
```

| 参数 | 类型 | 必填 |
|------|------|------|
| id | string | 是 |

---

### 3.4 图书列表（分页+筛选）

```
GET /article/getBookList
认证: 不需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |
| categoryId | string | 否 | 按分类筛选 |
| state | string | 否 | 按状态筛选（`可借阅`/`已借出`/`草稿`） |
| title | string | 否 | 按书名模糊搜索 |

**响应 data**：`PageBean<Article>`

---

### 3.5 图书详情

```
GET /article/detail
认证: 需要
```

| 参数 | 类型 | 必填 |
|------|------|------|
| id | string | 是 |

**响应 data**：`Article`

---

### 3.6 AI 生成图书简介

```
GET /article/getBookContent
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookName | string | 是 | 书名 |
| bookNum | string | 是 | ISBN |

**响应 data**：百度 ERNIE 生成的简介文本（String）

---

### 3.7 条形码识别

```
GET /article/getBookBarcode
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| base64 | string | 是 | 条形码图片文件名 |

> 图片需预先存放到 `booksys.barcode.base-path` 配置的目录

---

### 3.8 Python 条码识别

```
GET /article/getBookBarcodeByPy
认证: 需要
```

无参数，调用配置的 Python 脚本进行条码识别。

---

### 3.9 生成图书编号

```
GET /article/buildBookNumService
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isbn | string | 是 | ISBN 号 |

根据 ISBN 用 SHA-256 生成 6 位唯一图书编号。

---

### 3.10 生成二维码

```
GET /article/buildQrCode
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| isbn | string | 是 | ISBN 号 |

**响应 data**：二维码图片的 OSS URL（String）

---

### 3.11 图书总数

```
GET /article/getBookNumService
认证: 需要
```

**响应 data**：`Integer`

---

### 3.12 可借阅图书数

```
GET /article/getBookNumUseService
认证: 需要
```

**响应 data**：`Integer`

---

## 4. 借阅管理 /borrow

### 4.1 借阅图书

```
GET /borrow/borrowBook
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookId | int | 是 | 图书编号（bookNum） |
| day | int | 是 | 借阅天数 |

**业务规则**：
- 用户 state=2 时不可借阅
- 有逾期书籍未归还时不可借阅
- 最多同时借阅 6 本
- 图书已借出时不可重复借阅
- 使用 per-book 锁保证并发安全

**失败**：
| message |
|--------|
| 该用户当前不允许借阅 |
| 存在逾期书籍 |
| 借阅上限了哥们 |
| 该书籍已经借出 |

---

### 4.2 归还图书

```
POST /borrow/returnBook
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | int | 否 | 用户 ID |
| bookNum | string | 是 | 图书编号 |

---

### 4.3 借阅记录（分页）

```
GET /borrow/getRecord
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |
| bookName | string | 否 | 按书名筛选 |
| username | string | 否 | 按借阅人用户名筛选 |
| state | int | 否 | 按借阅状态筛选（1=借阅中/2=已归还/3=逾期） |

> 管理员可查看全部，读者只能看到自己的记录

**响应 data**：`PageBean<BorrowRecord>`

每条记录包含：
```json
{
  "id": 1,
  "borrowDate": "2026-01-01",
  "deadline": "2026-01-15",
  "borrowState": 1,
  "userId": 1,
  "lastDay": 5,
  "bookNum": "100001",
  "bookName": "法治的细节",
  "userName": "张三",
  "imageName": "封面URL"
}
```
| borrowState | 含义 |
|-------------|------|
| 1 | 借阅中 |
| 2 | 已归还 |
| 3 | 逾期 |

---

### 4.4 申请续借

```
GET /borrow/applyRenewal
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| bookNum | int | 是 | 图书编号 |
| day | int | 是 | 续借天数 |
| borrowRecordId | int | 是 | 借阅记录 ID |

> 同一借阅记录不能重复申请续借

---

### 4.5 续借申请列表

```
GET /borrow/getApplyRenewalList
认证: 需要
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 是 | 页码 |
| pageSize | int | 是 | 每页条数 |
| bookName | string | 否 | 按书名筛选 |
| username | string | 否 | 按申请人筛选 |

**响应 data**：`PageBean<ApplyRecord>`

```json
{
  "id": 1,
  "borrowRecordId": 5,
  "bookNum": "100001",
  "title": "法治的细节",
  "coverImg": "封面URL",
  "username": "张三",
  "userId": 1,
  "borrowDate": "2026-01-01",
  "deadline": "2026-01-15",
  "dayNum": 7,
  "applyState": 1,
  "msg": null
}
```

| applyState | 含义 |
|------------|------|
| 1 | 申请中 |
| 2 | 已通过 |
| 3 | 已拒绝 |

---

### 4.6 批准续借（管理员）

```
GET /borrow/passApply
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | int | 是 | 续借申请 ID |
| borrowRecordId | int | 是 | 借阅记录 ID |
| day | int | 是 | 续借天数 |
| deadline | string | 是 | 原归还日期（yyyy-MM-dd） |

---

### 4.7 拒绝续借（管理员）

```
GET /borrow/rejectApply
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 续借申请 ID |

> 拒绝后会自动向被拒用户推送消息（msg 字段）

---

### 4.8 编辑借阅记录（管理员）

```
POST /borrow/editRecord
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | int | 是 | 用户 ID |
| borrowId | int | 是 | 借阅记录 ID |
| date1 | datetime | 是 | 借阅时间 |
| date2 | datetime | 是 | 归还时间 |
| state | int | 是 | 借阅状态 |
| bookNum | int | 是 | 图书编号 |

> 会校验：借阅时间不能晚于当前时间，归还时间不能早于当前时间（若状态为"借阅中"）

---

### 4.9 催还提醒（管理员）

```
GET /borrow/urge
认证: 需要（管理员）
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 用户 ID |

> 向该用户发送催还邮件，并推送系统消息

---

### 4.10 借阅总数

```
GET /borrow/getAllBorrowNum
认证: 需要
```

**响应 data**：`Integer`

---

### 4.11 我的借阅数

```
GET /borrow/getMyRecordNumService
认证: 需要
```

**响应 data**：`Integer`

---

## 5. 分类管理 /category

### 5.1 新增分类

```
POST /category/add
认证: 需要
Content-Type: application/json
```

```json
{
  "categoryName": "文学",
  "categoryAlias": "wenxue"
}
```

> `categoryName` 和 `categoryAlias` 均为必填

---

### 5.2 查询全部分类

```
GET /category/query
认证: 不需要
```

**响应 data**：`List<Category>`

```json
[
  { "id": 1, "categoryName": "文学", "categoryAlias": "wenxue", "createUser": 1, "createTime": "2026-01-01 12:00:00", "updateTime": "2026-01-01 12:00:00" }
]
```

---

### 5.3 分类详情

```
GET /category/detail
认证: 需要
```

| 参数 | 类型 | 必填 |
|------|------|------|
| id | int | 是 |

**响应 data**：`Category`

---

### 5.4 更新分类

```
PUT /category
认证: 需要
Content-Type: application/json
```

```json
{
  "id": 1,
  "categoryName": "文学",
  "categoryAlias": "wenxue"
}
```
> `id` 必填

---

### 5.5 删除分类

```
DELETE /category
认证: 需要
```

| 参数 | 类型 | 必填 |
|------|------|------|
| id | int | 是 |

---

## 6. 文件上传 /load

```
POST /load
认证: 需要
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 上传的文件（最大 10MB） |

**成功响应**：
```json
{
  "code": 0,
  "message": "操作成功",
  "data": "https://ryan4real.oss-cn-guangzhou.aliyuncs.com/uuid.jpg"
}
```
> `data` 为上传后的文件 URL

**调用示例**：
```js
const formData = new FormData();
formData.append('file', fileInput.files[0]);
fetch('http://localhost:8080/load', {
  method: 'POST',
  body: formData
})
```

---

## 7. 数据结构定义（Schemas）

### 7.1 Result\<T\> — 统一响应

```typescript
interface Result<T> {
  code: number;    // 0=成功, 1=失败
  message: string; // 提示信息
  data: T;         // 负载数据
}
```

### 7.2 PageBean\<T\> — 分页数据

```typescript
interface PageBean<T> {
  total: number;   // 总记录数 (int64)
  items: T[];      // 当前页数据
}
```

### 7.3 User — 用户

```typescript
interface User {
  id: number;          // 必填
  username?: string;
  password?: string;   // 前端不显示（@JsonIgnore）
  nickname: string;    // 必填
  email: string;       // 必填
  userPic?: string;    // 头像URL
  createTime?: string; // datetime
  updateTime?: string; // datetime
  type?: number;       // 1=管理员, 2=读者
  state?: number;      // 1=可借阅, 2=不可借阅
  msg?: string;        // 系统消息（逾期提醒等）
}
```

### 7.4 Article — 图书

```typescript
interface Article {
  id?: number;
  title: string;       // 必填，1-10位非空
  content: string;     // 必填
  coverImg: string;    // 必填，封面URL
  state?: string;      // "已发布" | "草稿" | "可借阅" | "已借出"
  categoryId: number;  // 必填
  location?: string;   // 馆藏位置
  bookNum?: number;    // 图书编号（6位）
  createUser?: number; // 创建人ID
  createTime?: string; // datetime
  updateTime?: string; // datetime
  publisher?: string;  // 出版社
  isbn?: string;       // ISBN号
  isbnPhoto?: string;  // ISBN照片URL
}
```

### 7.5 Category — 分类

```typescript
interface Category {
  id: number;            // 更新时必填
  categoryName: string;  // 必填
  categoryAlias: string; // 必填
  createUser?: number;
  createTime?: string;   // "yyyy-MM-dd HH:mm:ss"
  updateTime?: string;   // "yyyy-MM-dd HH:mm:ss"
}
```

### 7.6 BorrowRecord — 借阅记录

```typescript
interface BorrowRecord {
  id?: number;
  borrowDate?: string;  // "yyyy-MM-dd"
  deadline?: string;    // "yyyy-MM-dd"
  borrowState?: number; // 1=借阅中, 2=已归还, 3=逾期
  userId?: number;
  lastDay?: number;     // 剩余天数
  bookNum?: string;     // 图书编号
  bookName?: string;    // 书名（服务端填充）
  userName?: string;    // 借阅人用户名（服务端填充）
  imageName?: string;   // 图书封面URL（服务端填充）
}
```

### 7.7 ApplyRecord — 续借申请

```typescript
interface ApplyRecord {
  id?: number;
  borrowRecordId?: number; // 借阅记录ID
  bookNum?: string;        // 图书编号
  title?: string;          // 书名
  coverImg?: string;       // 封面URL
  username?: string;       // 申请人用户名
  userId?: number;
  borrowDate?: string;     // "yyyy-MM-dd"
  deadline?: string;       // "yyyy-MM-dd"
  dayNum?: number;         // 申请续借天数
  applyState?: number;     // 1=申请中, 2=已通过, 3=已拒绝
  msg?: string;            // 申请消息
}
```

---

## 附录 A：前端对接清单

### A.1 免登录页面需要的接口

| 页面 | 接口 |
|------|------|
| 登录页 | `POST /user/login` |
| 注册页 | `POST /user/register` + `GET /user/sendmail` |
| 忘记密码 | `POST /user/editPswByEmail` + `GET /user/sendmail` |
| 图书浏览页 | `GET /article/getBookList` |
| 分类列表 | `GET /category/query` |

### A.2 读者端需要的接口（需登录）

| 功能 | 接口 |
|------|------|
| 个人信息 | `GET /user/userInfo` + `PUT /user/update` |
| 修改密码 | `PATCH /user/updatePwd` |
| 上传头像 | `POST /load` → `PATCH /user/updateAvatar` |
| 图书详情 | `GET /article/detail` |
| 借阅图书 | `GET /borrow/borrowBook` |
| 归还图书 | `POST /borrow/returnBook` |
| 我的借阅记录 | `GET /borrow/getRecord` |
| 申请续借 | `GET /borrow/applyRenewal` |
| 续借申请记录 | `GET /borrow/getApplyRenewalList` |
| 系统消息 | `GET /user/deleteUserMsg` |
| 清除消息 | `GET /user/deleteUserMsg` |

### A.3 管理员端额外需要的接口

| 功能 | 接口 |
|------|------|
| 用户管理 | `GET /user/getUserList` + `PATCH /user/editUser` + `GET /user/deleteUser` |
| 借阅状态管理 | `GET /user/upgradeUserState` |
| 图书管理 | `POST/PUT/DELETE /article` |
| 分类管理 | `POST/PUT/DELETE /category` |
| 借阅记录管理 | `POST /borrow/editRecord` |
| 续借审批 | `GET /borrow/passApply` + `GET /borrow/rejectApply` |
| 催还 | `GET /borrow/urge` |
| 统计 | `GET /user/getUserNumService` + `GET /borrow/getAllBorrowNum` + `GET /article/getBookNumService` |

### A.4 典型页面流程

**借书流程**：
```
1. GET /article/getBookList → 展示图书列表
2. GET /article/detail?id=xxx → 查看图书详情
3. GET /borrow/borrowBook?bookId=xxx&day=30 → 借阅
4. GET /borrow/getRecord → 查看借阅记录
```

**还书流程**：
```
1. GET /borrow/getRecord → 查看我的借阅记录
2. POST /borrow/returnBook?bookNum=xxx → 归还
```

**续借流程（需管理员审批）**：
```
1. GET /borrow/applyRenewal?bookNum=xxx&day=7&borrowRecordId=5 → 申请续借
2. [管理员] GET /borrow/getApplyRenewalList → 查看申请列表
3. [管理员] GET /borrow/passApply 或 /borrow/rejectApply → 审批
```

---

## 附录 B：环境变量参考

部署时需设置的环境变量（本地开发用 `application.yml` 默认值即可）：

```bash
DB_USERNAME=root
DB_PASSWORD=root
DB_URL=jdbc:mysql://localhost:3306/cov?...
MAIL_USERNAME=your@163.com
MAIL_PASSWORD=your_smtp_password
OSS_ACCESS_KEY_ID=...
OSS_ACCESS_KEY_SECRET=...
BAIDU_AI_API_KEY=...
BAIDU_AI_SECRET_KEY=...
JWT_SECRET=strong_secret_key
DRUID_USERNAME=admin
DRUID_PASSWORD=secure_password
```
