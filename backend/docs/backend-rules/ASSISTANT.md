# 后端助手入口

本文是编码助手处理 `rf` 后端任务时的入口。先判断本次任务类型，再读取对应专题规则。

## 读取原则

- 先读仓库根目录 `AGENTS.md`，再读本文件。
- 优先沿用当前模块已有代码模式；专题规则用于补齐边界和冲突判断。
- 如果用户当前明确要求与本文不同，以用户当前要求为准。
- 如果专题规则之间出现冲突，以更具体的业务模块约定优先。

## 分层提醒

- 后端服务采用 `api` / `provider` 双模块；`api` 只放 RPC 契约，`provider` 放业务实现。
- `provider` 顶层只保留 `interfaces`、`application`、`domain`、`infrastructure`、`common`。
- `interfaces` 只做入口适配，业务编排放 `application.manager`。
- 单表持久化通过 `application/port/persistence` 暴露端口，实现放在 `infrastructure/persistence` 或 `infrastructure/port/persistence` 下。
- Mapper XML 和注解 SQL 禁止 `JOIN` / 联表查询；跨表数据在 Java 层分步查询和组装。

## 任务路由

| 任务类型 | 必读专题 |
| --- | --- |
| 普通后端 CRUD、列表、详情、保存、删除 | `core-ddd.md`、`persistence.md`、`naming-and-quality.md` |
| 新增或修改 HTTP Controller | `core-ddd.md`、`api-contract.md`、`naming-and-quality.md` |
| 新增或修改 Dubbo API / RemoteServiceImpl | `core-ddd.md`、`api-contract.md`、`naming-and-quality.md` |
| Mapper、Entity、SQL、分页、批量写入 | `persistence.md`、`naming-and-quality.md` |
| Gateway、短信、图形验证码、机器人、Job、Redis、MQ | `core-ddd.md`、`api-contract.md`、`naming-and-quality.md` |
| 代码审查、规范化或重构 | `checklist.md`，并按涉及范围读取对应专题 |

## 完成前检查

改动完成后读取 `checklist.md`，只按本次任务涉及的清单自检。后端 Java 改动至少执行受影响模块的 `compileJava`。
