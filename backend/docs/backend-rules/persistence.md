# 持久化规则

## Mapper、Entity、XML

Mapper、Mapper XML、Entity 按业务上下文拆包。单数据源模块可使用简单上下文命名：

```text
infrastructure/persistence/{context}/mapper/XxxMapper.java
infrastructure/persistence/{context}/entity/XxxEntity.java
src/main/resources/mapper/{context}/XxxMapper.xml
```

多数据源模块按数据源命名空间扩展，例如 `rf-mng` 使用 `platform` 和 `robot`：

```text
infrastructure/persistence/{datasource}/{context}/mapper/XxxMapper.java
src/main/resources/mapper/{datasource}/{context}/XxxMapper.xml
```

Mapper XML 的 `namespace` 必须指向对应 Mapper，`resultMap type` 和 `parameterType` 必须指向对应 Entity 或查询对象。

## PersistencePort

应用层访问单表持久化能力时，统一通过 `PersistencePort`，不要在 `application` 直接依赖 Mapper、Entity、DbQuery。

推荐路径：

```text
application/port/persistence/{context}/XxxPersistencePort.java
application/port/persistence/{context}/data/*Data.java
application/port/persistence/{context}/record/*Record.java
infrastructure/persistence/{context}/impl/*PersistencePortImpl.java
```

职责约束：

- `PersistencePort` 表达单表读写能力，不承载业务流程。
- `data` 放应用层写入数据；`record` 放应用层读取结果。
- `PersistencePortImpl` 负责调用 Mapper，并完成 `Data/Record` 与 `Entity` 的转换。
- 事务边界由 Manager 或应用层事务组件控制，不下沉到 `PersistencePortImpl`。

## SQL 底线

- Mapper XML 和注解 SQL 必须保持简单、直接，禁止使用 `JOIN` / 联表查询。
- 跨表列表或导出先查主表，再收集关联键批量查询从表，最后在 Manager 或 Assembler 中组装。
- 非分页查询优先拆成语义明确的专用方法，例如 `getById`、`listByIds`、`listByTaskId`。
- 不要用大量 `<if>` 拼成一个承载多种业务场景的通用 SQL。
- 没有业务展示顺序或分页要求时，不添加无意义 `ORDER BY`。
- 禁止在循环中执行单条 SQL、RPC、HTTP、Redis 等 IO。
- 同一张表的批量查询优先使用条件批量查询，批量写入优先使用批量提交能力。

## 时间字段

数据库已定义默认值或自动更新时间的字段，由数据库维护：

- `created_at`
- `updated_at`
- `gmt_create`
- `gmt_modified`

这类字段不要在 INSERT 列表、UPDATE 语句或 Java 写入对象中手动赋值，例如不要写 `updated_at = NOW()`。

业务事件时间字段可以由业务逻辑显式维护，例如：

- `last_confirm_time`
- `last_feedback_time`
- `last_adjust_time`
- `handled_at`
- `sms_send_time`

判断标准是：如果字段表达数据库记录生命周期，交给数据库；如果字段表达某个业务动作发生时间，由业务代码写入。

## 查询缺失处理

Mapper 和 PersistencePort 的普通按 ID 查询方法只负责读取数据，查不到时返回 `null` 或空集合，不直接抛业务异常。是否将“不存在”视为参数错误、数据异常或允许为空，由 Manager 根据当前用例决定。
