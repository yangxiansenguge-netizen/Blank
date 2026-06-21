# Blank - 明信片社交应用

Blank 是一个"明信片"社交应用。用户可以创建带有邮票、贴纸和文字的个性化明信片，发送给好友或投入漂流瓶传递给陌生人。应用包含 AI 内容审核、VIP 会员、每日签到积分、好友系统等完整功能。

## 功能特性

- **明信片创作** — 图片 + 邮票 + 文字 + 贴纸，拖拽编辑，所见即所得
- **发送与漂流** — 指定收件人发送，或投入漂流瓶随机传递
- **漂流共创** — 多人可在漂流明信片上添加文字和贴纸接力创作
- **定时发送** — 支持预约发送时间，到达后自动投递
- **AI 内容审核** — 阿里云 DashScope 自动审核图片和文字，违规内容提交人工复审
- **AI 智能助手** — 一键润色文字、根据图片生成文案、自定义 AI 指令
- **邮票商店** — 多系列邮票，每日购买，消耗邮寄明信片
- **VIP 会员** — 包月/包季/包年/终身，免邮分寄明信片，AI 免费使用
- **签到系统** — 每日签到 + 明信片任务奖励
- **好友系统** — 搜索、申请、接受、删除好友
- **评论点赞** — 明信片评论互动，置顶、点赞
- **管理后台** — 明信片管理、邮票系列管理、激活码生成、人工审核

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 (Composition API) + TypeScript |
| 构建工具 | Vite 6 |
| UI 组件 | Element Plus + TailwindCSS 4 |
| 后端框架 | Spring Boot 2.6.13 + MyBatis |
| 数据库 | MySQL 8 |
| 缓存 | Redis |
| 认证 | Spring Security + JWT |
| 文件存储 | 阿里云 OSS |
| AI 服务 | 阿里云 DashScope (qwen3.5-flash / qwen-turbo) |
| 邮件 | Spring Mail (JavaMailSender) |
| 支付 | ZPAY 易支付 |

## 项目结构

```
Blank/
├── back/                  # Node.js 后端（原版）
├── blank-server/          # Spring Boot 后端（新版）
│   ├── src/main/java/com/blank/app/
│   │   ├── config/        # Spring 配置（Security/MyBatis/Redis/Web）
│   │   ├── security/      # JWT 认证（Provider/Filter/UserDetails）
│   │   ├── entity/        # 数据实体（15 张表）
│   │   ├── dto/           # 请求/响应 DTO
│   │   ├── mapper/        # MyBatis Mapper 接口
│   │   ├── service/       # 业务服务层
│   │   ├── controller/    # REST 控制器（12 个模块）
│   │   ├── util/          # 工具类
│   │   └── exception/     # 全局异常处理
│   └── src/main/resources/
│       ├── application.yml           # 主配置文件
│       ├── application-secret.yml    # 敏感配置（Git 忽略）
│       ├── init.sql                  # 数据库建表脚本
│       └── mapper/                   # MyBatis XML 映射文件
├── front/                 # Vue 3 前端
│   ├── src/
│   │   ├── pages/         # 页面组件
│   │   ├── api/           # API 请求封装
│   │   ├── store/         # 状态管理
│   │   ├── router/        # 路由配置
│   │   └── utils/         # 工具函数
│   └── vite.config.ts
└── 参考/                   # 项目文档
```

## 快速开始

### 环境要求

- **JDK 11**
- **MySQL 8**
- **Redis**
- **Maven 3.6+**
- **Node.js 18+ / pnpm**

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS blank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
mysql -u root -p blank < blank-server/src/main/resources/init.sql
```

### 2. 配置敏感信息

复制并编辑配置文件：

```bash
cp blank-server/src/main/resources/application-secret.yml.example blank-server/src/main/resources/application-secret.yml
```

编辑 `application-secret.yml` 填入你的：

```yaml
spring:
  datasource:
    username: your_mysql_user
    password: your_mysql_password
  redis:
    password: your_redis_password
  mail:
    username: your_email@163.com
    password: your_smtp_auth_code

app:
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: LTAI5t...
    access-key-secret: ...
    bucket-name: blank-img
  ai:
    dashscope-api-key: sk-...
```

### 3. 启动后端

```bash
cd blank-server
mvn clean spring-boot:run
```

后端默认运行在 `http://localhost:8004`

### 4. 启动前端

```bash
cd front
pnpm install
pnpm dev
```

前端默认运行在 `http://localhost:3004`

### 5. 设置为管理员

```bash
mysql -u root -p blank -e "UPDATE users SET identity = 'admin' WHERE id = 1"
```

然后访问 `http://localhost:3004/manager` 进入管理后台。

## API 模块

| 前缀 | 功能 | 端点数 |
|------|------|--------|
| `/api/auth` | 注册/登录/登出/验证码/密码重置 | 6 |
| `/api/user` | 用户资料/头像/密码/搜索 | 7 |
| `/api/postcards` | 明信片 CRUD/发现/漂流/点赞/收件箱 | 14 |
| `/api/stamps` | 邮票商店/购买/我的邮票 | 4 |
| `/api/friends` | 好友申请/接受/删除/搜索 | 6 |
| `/api/checkin` | 每日签到/明信片任务奖励 | 3 |
| `/api/comments` | 评论/置顶/点赞 | 5 |
| `/api/ai` | AI 润色/图片生成文案/自定义 | 3 |
| `/api/admin` | 管理后台/审核/激活码 | 20 |
| `/api/notifications` | 通知列表/已读/未读数 | 4 |
| `/api/vip` | VIP 计划/支付/激活码兑换 | 7 |

## 响应格式

```json
// 成功
{ "code": 0, "message": "success", "data": {...} }

// 分页
{ "code": 0, "message": "success", "data": {
    "list": [...],
    "pagination": { "total": 100, "page": 1, "pageSize": 20, "totalPages": 5 }
}}

// 错误
{ "code": -1, "message": "错误信息", "data": null }
```

## 关键业务规则

- **明信片创建**：消耗 1 枚邮票，获得 20 邮分，异步 AI 审核
- **VIP 免邮**：VIP 用户寄明信片不消耗邮票
- **邮票每日限购**：每张邮票每天限购 1 次
- **注册奖励**：新用户 100 邮分 + 随机默认头像
- **签到**：每天 10 邮分
- **明信片任务**：每天发送明信片额外奖励 20 邮分（VIP 30）
- **AI 费用**：文字润色 5 邮分，图片生成 10 邮分（VIP 免费）

## License

MIT
