# rf 编码助手入口

本文是编码助手进入 `rf` 仓库后的总入口。执行代码、配置、测试、提交等具体操作前，必须先读取本文，再按本次任务范围读取对应子目录规则。

## 语言约束

本仓库的 Markdown 文档、技术方案、开发说明和代码注释默认使用中文。

## 任务入口

| 任务范围 | 必读规则 |
| --- | --- |
| 后端 Java、SQL、Dubbo、Job、Gateway、部署配置 | `backend/docs/backend-rules/ASSISTANT.md` |
| 管理后台前端 `rf-mng-node` | `frontend/rf-mng-node/docs/frontend-rules/ASSISTANT.md` |
| 员工端 H5 `rf-h5-node` | `frontend/rf-h5-node/docs/frontend-rules/ASSISTANT.md` |
| Docker Compose 测试部署 | `deploy/test/README.md` |
| 生产 K8s / 云效部署 | `deploy/prod/README.md` |

如果任务同时涉及多个范围，按影响范围分别读取规则。若规则文件指向专题文档，继续读取本次任务命中的专题后再动手。

## 冲突优先级

1. 用户当前明确要求。
2. 当前模块已有代码模式。
3. 子目录规则和专题规范。
4. 本文通用约束。

当用户要求与既有规范冲突时，先说明影响，再按用户确认后的方向执行。

## 项目边界

- `backend/common`：后端公共技术能力。
- `backend/services/rf-mng`：管理端服务，对外提供管理 API，并通过 RPC 调用员工绩效模块。
- `backend/services/rf-performance`：员工绩效服务，采用 `api` / `provider` 双模块。
- `frontend/rf-mng-node`：管理后台。
- `frontend/rf-h5-node`：公众号 H5 员工端。
- `deploy/test`：测试环境 Docker Compose 部署。
- `backend/k8s/prod`、`frontend/*/k8s/prod`、`deploy/prod`：生产 K8s 与云效流水线配置。

## 通用约束

- 后端参考 `/Users/zzy/code/zyqy/zy_qy/backend` 的 DDD 分层风格。
- SQL 必须保持简单、直接，Mapper XML 或注解 SQL 禁止 `JOIN` / 联表查询；跨表数据在 Java 应用层分步查询并组装。
- 数据库已定义默认值和 `ON UPDATE CURRENT_TIMESTAMP` 的时间字段，不在 SQL 和 Java 写入逻辑中手动维护；业务事件时间字段除外。
- 调用内部系统或第三方系统统一使用 Gateway 命名，不使用 Service 命名承载外部调用。
- Java 类、方法、字段需要中文注释。
- Spring 依赖注入统一使用 `javax.annotation.Resource`。
- 变更已有文件时不得回滚用户或其他任务已经产生的无关改动。

## Git 分支命名

新功能分支使用 `feat/yyyyMMdd-feature`，缺陷修复分支使用 `fix/yyyyMMdd-feature`，日期使用创建分支当天，功能名使用小写短横线。

## 常用验证

后端按影响模块执行：

```bash
cd backend
./gradlew :services:rf-mng:rf-mng-provider:compileJava
./gradlew :services:rf-performance:rf-performance-provider:compileJava
```

前端按影响项目执行：

```bash
cd frontend/rf-mng-node
npm run build

cd frontend/rf-h5-node
npm run build
```
