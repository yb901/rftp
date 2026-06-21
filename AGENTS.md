# AGENTS.md

## 语言约束

本仓库的 Markdown 文档、技术方案、开发说明和代码注释默认使用中文。

## 架构约束

后端参考 `/Users/zzy/code/zyqy/zy_qy/backend` 的 DDD 风格：

- `rfpt-mng-api` 放对外契约和 DTO。
- `rfpt-mng-provider` 只保留 `interfaces`、`application`、`domain`、`infrastructure`、`common` 五类顶层包。
- Controller 只做参数适配和返回封装，业务编排放 Manager。
- 单表持久化通过 PersistencePort 隔离。
- SQL 必须保持简单、直接，禁止在 Mapper XML 或注解 SQL 中使用 `JOIN` / 联表查询；跨表信息通过单表批量查询后在 Java 应用层组装。
- 查询结果需要补充关联信息时，先按主表分页或列表查询，再收集关联键批量查询从表，最后在 Manager / Assembler 中回填，避免在 SQL 中承载业务组装。
- 调用 `tax-browser-worker` 这类内部系统使用 Gateway 命名，不使用 Service 命名。
- Java 类、方法、字段需要中文注释。
- 依赖注入优先使用 `javax.annotation.Resource`。

## Git 分支命名

新建功能分支使用 `feature/yyyyMMdd-feature-name`。
