# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Blank 是一个"明信片"社交应用，用户可以创建、发送带有邮票和贴纸元素的明信片给好友或漂流传递，包含 AI 内容审核、VIP 会员、签到积分等系统。

## Commands

### Backend (`blank-server/`)
```bash
cd blank-server
mvn clean spring-boot:run    # 开发启动（默认端口 8004）
mvn clean package -DskipTests # 生产打包 JAR
```

### Frontend (`front/`)
```bash
cd front
pnpm install        # 安装依赖
pnpm dev            # Vite 开发服务器（端口 3004）
pnpm build          # 生产构建
pnpm lint           # TypeScript 类型检查（tsc --noEmit）
pnpm preview        # 预览生产构建
```

### Database
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS blank CHARACTER SET utf8mb4"
mysql -u root -p blank < blank-server/src/main/resources/init.sql
mysql -u root -p blank -e "UPDATE users SET identity = 'admin' WHERE id = 1"
```

## Architecture

### Backend (Spring Boot 2.6.13 + MyBatis + Redis + OSS)

**入口**: `BlankApplication.java` — `@SpringBootApplication` + `@MapperScan`

**分层**:
- `config/` — SecurityConfig/WebConfig/RedisConfig/ScheduledTaskConfig
- `security/` — JwtTokenProvider/JwtAuthenticationFilter/JwtUserDetails/CurrentUser
- `entity/` — 15 个实体类，纯 POJO
- `dto/` — request/response DTO
- `mapper/` — MyBatis @Mapper 接口 + XML 映射文件
- `service/impl/` — 业务逻辑实现
- `controller/` — 12 个 REST 控制器
- `util/` — ResponseUtils/CodeGenerator/ZPaySignUtil/RedisKeyBuilder
- `exception/` — BusinessException + GlobalExceptionHandler

**API（共 66 个端点，全部 `/api/` 下）**:
| 路由 | 功能 |
|------|------|
| `/api/auth` | 注册/登录/登出/验证码/密码重置 |
| `/api/user` | 用户资料/头像/密码/搜索 |
| `/api/postcards` | 明信片 CRUD/发现/漂流/点赞/收发件箱 |
| `/api/stamps` | 邮票商店/购买/我的邮票 |
| `/api/friends` | 好友申请/接受/删除 |
| `/api/checkin` | 每日签到/任务奖励 |
| `/api/comments` | 评论/置顶/点赞 |
| `/api/ai` | AI 润色/图片生成/自定义 |
| `/api/admin` | 管理后台/审核/激活码 |
| `/api/notifications` | 通知列表/已读/未读数 |
| `/api/vip` | VIP 计划/支付/激活码 |

**认证**: Spring Security + JWT，`JwtAuthenticationFilter` 拦截，Redis 黑名单退登

**响应格式**: `{ code: 0/-1, message: string, data: ... }`

**关键业务**: VIP 免邮 | AI 异步审核(fail-open) | 每日签到+任务 | 邮票限购(Redis) | 定时发送(60s轮询) | OSS 文件上传

**数据库**: `init.sql` 含完整 15 张表，无需运行时迁移

### Frontend (Vue 3 + Vite + TailwindCSS + Element Plus)

与 `back/` 原 Node.js 版本对应关系一致。

## Tech Stack

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite 6 + Element Plus + TailwindCSS 4 |
| 后端 | Spring Boot 2.6.13 |
| ORM | MyBatis（手写 XML） + PageHelper |
| 数据库 | MySQL 8 |
| 缓存 | Redis |
| 认证 | Spring Security + JWT |
| 文件 | 阿里云 OSS + 本地 fallback |
| AI | DashScope (qwen3.5-flash / qwen-turbo) |
| 邮件 | Spring Mail |
| 支付 | ZPAY + MD5 签名 |
