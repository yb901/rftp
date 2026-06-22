# 命名与质量规则

## 固定后缀

| 层级 | 对象类型 | 命名规则 |
| --- | --- | --- |
| `api` | RPC 请求入参 | `*Param` |
| `api` | RPC 查询入参 | `*Query` 或 `*PageParam` |
| `api` | RPC 返回对象 | `*Dto` |
| `interfaces` | HTTP 请求入参 | `*CtrlParam` |
| `interfaces` | HTTP 返回对象 | `*Vo` |
| `application` | 写操作入参 | `*Command` |
| `application` | 查询入参 | `*Query` |
| `application` | 返回对象 | `*Result` |
| `application/port/persistence` | 写入数据 | `*Data` |
| `application/port/persistence` | 读取结果 | `*Record` |
| `infrastructure/persistence` | 数据库对象 | `*Entity` |
| `application/port/gateway` | 下游请求入参 | `*GatewayParam` |
| `application/port/gateway` | 下游返回对象 | `*GatewayResult` |

不要为了少写对象让 API 契约、HTTP 对象、应用对象、持久化对象互相继承或跨层复用。

## 对象转换

转换只发生在边界处：

```text
HTTP CtrlParam / RPC Param
    -> Command / Query
    -> Data / Domain Model
    -> Entity

Entity / Domain Model
    -> Record / Result
    -> HTTP Vo / RPC Dto
```

字段重命名、派生字段、枚举展示和跨表组装应放在 Converter 或 Assembler，不散落在 Controller。

## 依赖注入

Spring Bean 注入统一使用 `javax.annotation.Resource`。

禁止使用：

```java
import org.springframework.beans.factory.annotation.Autowired;
```

标准写法：

```java
import javax.annotation.Resource;

@Resource
private XxxManager xxxManager;
```

## 事务

所有 `@Transactional` 必须显式指定 `transactionManager`，主库事务管理器统一使用：

```java
@Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
```

后续增加多数据源时，第二库事务管理器按 `{datasource}TransactionManager` 命名。

## 中文注释

所有 Java 类、方法、字段都必须有中文注释。

- 类注释说明对象职责。
- 方法注释说明业务动作或转换目的。
- 字段注释说明业务含义。
- 字段值如果对应枚举，字段注释中用 `@see` 指向枚举类。
- 编排方法里的关键步骤可以写中文行内注释，描述业务动作。

## 基础质量

- `Serializable` 类统一使用稳定的 `serialVersionUID = 1L`。
- 分页入参优先继承公共分页对象，不重复定义 `page`、`size`。
- 前端可见数据库 ID 按项目公共能力进行编码/解码，避免裸露内部 Long ID。
- 方法优先使用中断式编码，减少深层嵌套。
- 外部入参、列表、分页对象、聚合子对象都要做空值保护。
- 调用 manager、gateway、mapper 后，先单独放一行再处理结果，方便调试。
- 容器日志目录通过 `LOG_PATH` 环境变量覆盖，K8s 中统一写入 `/app/logs`。
