# 后端自检清单

按本次任务涉及的范围检查，不需要机械套用所有条目。

## 分层与对象

- 新增类是否放在 `interfaces`、`application`、`domain`、`infrastructure`、`common` 的正确层级。
- `api` 模块是否只包含对外契约对象。
- Controller、RemoteServiceImpl、Job 是否只做入口适配、基础校验和对象转换。
- Manager 是否承担用例编排和事务边界。
- Manager 是否只接收 `Command` / `Query`，只返回应用层 `Result`。
- Entity、Param、Vo、Dto 是否没有跨越错误边界。

## 持久化与 SQL

- Mapper XML 或注解 SQL 是否没有 `JOIN` / 联表查询。
- 跨表展示或导出是否通过 Java 层分步查询和组装。
- `application` 是否没有直接依赖 Mapper、Entity、DbQuery。
- 普通按 ID 查询方法是否只返回空结果，不直接抛业务异常。
- 是否不存在循环内 SQL/RPC/HTTP/Redis IO。
- 数据库自动维护的 `created_at`、`updated_at`、`gmt_create`、`gmt_modified` 是否没有被手动写入。
- 业务事件时间字段是否仍由业务动作显式维护。

## 集成与入口

- 下游调用是否封装为 Gateway。
- 内部系统 Gateway 是否放在 `internal`，第三方公网能力是否放在 `external`。
- XXL-JOB、MQ、Callback 是否只做入口适配，复杂流程进入 Manager。
- Redis key 是否集中管理。

## 质量

- Java 类、方法、字段中文注释是否完整。
- 是否没有使用 `Autowired`，依赖注入是否使用 `javax.annotation.Resource`。
- `@Transactional` 是否显式指定 `transactionManager`。
- 分页参数是否复用公共分页对象。
- `Serializable` 类是否声明稳定的 `serialVersionUID = 1L`。
- 后端服务 `logback-spring.xml` 是否支持 `LOG_PATH` 环境变量。
