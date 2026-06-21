# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Blank 是一个"明信片"社交应用，用户可以创建、发送带有邮票和贴纸元素的明信片给好友或漂流传递，包含 AI 内容审核、VIP 会员、签到积分等系统。

## Commands

### Backend (`back/`)
```bash
cd back
pnpm dev           # 启动开发服务器 (nodemon, 默认端口 3001, 但实际 .env 可能改为 3003)
pnpm start         # 生产启动
```

### Frontend (`front/`)
```bash
cd front
pnpm dev           # Vite 开发服务器 (端口 3000)
pnpm build         # 生产构建
pnpm lint          # TypeScript 类型检查 (tsc --noEmit)
pnpm preview       # 预览生产构建
```

### Database
```bash
# 初始化数据库（MySQL 8）
mysql -u root -p < back/init.sql
```

## Architecture

### Backend (Express + MySQL + Redis)

**入口**: `back/app.js` — 加载环境变量、注册中间件和路由、调用 `ensureAppSchema()` 后启动服务。

**分层结构**:
- `config/` — MySQL 连接池 (`db.js`)、Redis (`redis.js`，key 前缀 `blank:`)、邮件 (`mail.js`)
- `routes/` — Express 路由层，每个模块对应一组 API 端点（注意 `routes/ai.js` 将控制器逻辑内联在路由文件中）
- `controllers/` — 业务逻辑处理函数
- `middleware/` — `auth.js`（JWT 验证、可选认证、管理员验证）、`upload.js`（Multer 文件上传）、`errorHandler.js`
- `services/` — 独立业务模块：`moderation.js`（AI 内容审核）、`vipService.js`（VIP 状态管理、支付签名）
- `utils/` — `response.js`（统一 JSON 响应格式 `{code, message, data}`）、`jwt.js`、`helpers.js`、`schema.js`（数据库表结构自动创建/迁移）

**API 路由**（全部挂载在 `/api/` 下）:
| 路由前缀 | 功能 |
|----------|------|
| `/api/auth` | 注册、登录、登出、验证码、密码重置 |
| `/api/user` | 用户资料、头像上传 |
| `/api/postcards` | 明信片 CRUD、发现/漂流列表、点赞、收藏、收发件箱 |
| `/api/stamps` | 邮票商店、购买、我的邮票 |
| `/api/friends` | 好友申请/接受/删除/搜索 |
| `/api/checkin` | 每日签到领邮分 |
| `/api/comments` | 明信片评论及点赞 |
| `/api/ai` | AI 生成内容接口 |
| `/api/admin` | 管理员功能（审核明信片等） |
| `/api/notifications` | 用户通知 |
| `/api/vip` | VIP 订阅/购买/激活码 |

**认证体系**:
- JWT（`jsonwebtoken`），token 由 `utils/jwt.js` 签发
- 登出时 token 加入 Redis 黑名单（`token_blacklist:*`），有效期与 JWT 一致
- `auth` 中间件：必须登录；`optionalAuth`：可选登录（公开内容同时标记用户点赞状态）；`adminAuth`：需要 admin 身份

**统一响应格式** (`utils/response.js`):
```js
// 成功: { code: 0, message, data }
// 分页: { code: 0, message, data: { list, pagination: { total, page, pageSize, totalPages } } }
// 错误: { code: -1, message, data: null }
```

**VIP 系统**:
- 四种订阅计划：monthly(包月)/quarterly(包季)/yearly(包年)/lifetime(终身会员)
- 集成 ZPAY 易支付，在 `services/vipService.js` 中处理签名和订单验证
- `hasVipAccess()` 判断用户是否为 VIP（VIP 用户寄明信片不消耗邮票）

**AI 内容审核** (`services/moderation.js`):
- 使用阿里 DashScope API（qwen3.5-flash/qwen-turbo）
- 创建明信片后异步审核图片和文字，通过则 `status='sent'`，不通过则 `status='pending'` 进入人工审核
- API 调用失败时默认放行，避免阻塞用户

**定时任务** (`postcardController.js`):
- `startScheduledPostcardProcessor()` 每 60 秒处理到达发送时间的定时明信片，将其状态从 `scheduled` 改为 `reviewing` 并触发审核

**数据库迁移**: `utils/schema.js` 中 `ensureAppSchema()` 在服务启动时执行，自动创建/修改表结构（幂等操作），包括 users 扩展字段、VIP 相关表、通知表等。

### Frontend (Vue 3 + Vite + TailwindCSS + Element Plus)

**入口**: `front/src/main.ts` — 注册 Vue Router、Element Plus、暗色模式检测

**页面** (`pages/`):
- `Home.vue` — 首页（发现广场）
- `Mail.vue` — 明信片广场（漂流+发现）
- `Create.vue` — 创建明信片编辑器
- `Shop.vue` — 邮票商店
- `Profile.vue` / `Vip.vue` / `CheckIn.vue` / `MyStamps.vue` / `Favorites.vue` / `Outbox.vue` / `PostDetail.vue`
- `Login.vue` / `Register.vue` / `ForgotPassword.vue` / `Settings.vue`
- `manager/ManagerDashboard.vue` — 管理后台（需 admin 权限）

**前端 API 层** (`api/`): 每个模块对应后端路由，通过 `utils/request.js` 中的 axios 实例发送请求，自动附加 JWT token 和全局 loading 状态管理。

**状态管理** (`store/`): 使用 Vue 3 composables（非 Pinia），包括 `user.ts`（用户认证状态）、`checkin.ts`（签到状态）、`mailAlert.ts`

**路由守卫** (`router/index.ts`): 前置守卫检查 token、管理员权限，无权限跳转登录页

## Tech Stack Summary

| 层级 | 技术 |
|------|------|
| 前端框架 | Vue 3 (Composition API) + TypeScript |
| 前端构建 | Vite 6 |
| UI 库 | Element Plus + TailwindCSS 4 |
| 路由 | Vue Router 5 |
| 后端框架 | Express 4 |
| 数据库 | MySQL 8 (mysql2) |
| 缓存 | Redis (ioredis) |
| 认证 | JWT (jsonwebtoken) + bcryptjs |
| 文件上传 | Multer |
| 邮件 | Nodemailer |
| AI 审核 | DashScope (qwen3.5-flash / qwen-turbo) |
| 支付 | ZPAY 易支付 |
| 包管理器 | pnpm |
