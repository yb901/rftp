# AGENTS.md

## 语言约束

本仓库的 Markdown 文档、技术方案、开发说明和代码注释默认使用中文。

## 架构约束

后端参考 `/Users/zzy/code/zyqy/zy_qy/backend` 的 DDD 风格：

- `rfpt-mng-api` 放对外契约和 DTO。
- `rfpt-mng-provider` 只保留 `interfaces`、`application`、`domain`、`infrastructure`、`common` 五类顶层包。
- Controller 只做参数适配和返回封装，业务编排放 Manager。
- 单表持久化通过 PersistencePort 隔离。
- 调用 `tax-browser-worker` 这类内部系统使用 Gateway 命名，不使用 Service 命名。
- Java 类、方法、字段需要中文注释。
- 依赖注入优先使用 `javax.annotation.Resource`。

## Git 分支命名

新建功能分支使用 `feature/yyyyMMdd-feature-name`。
