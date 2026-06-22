# 后端核心分层规则

## 模块边界

后端服务默认采用 `api` / `provider` 双模块。

- `api`：对外 Dubbo 契约，只放 `remoteservice`、`param`、`query`、`dto` 和需要暴露的 `enums`。
- `provider`：业务实现模块，顶层只放 `interfaces`、`application`、`domain`、`infrastructure`、`common`。

`provider` 顶层不要直接平铺业务包。业务包必须下沉到具体职责包下面，例如：

```text
interfaces/controller/performance
interfaces/remoteserviceimpl/performance
application/manager/performance
application/port/persistence/performance
infrastructure/persistence/performance
```

## 分层职责

- `interfaces`：HTTP、Dubbo、Job、MQ、Callback 等入口适配，负责参数转换、基础校验和统一响应包装。
- `application`：用例编排、事务边界、跨表数据组装、应用对象转换、出站端口契约。
- `domain`：核心业务规则、业务枚举、领域对象和领域服务。领域层禁止依赖 Spring、Dubbo、MyBatis、Redis、HTTP/RPC 对象、Entity、Mapper。
- `infrastructure`：数据库、Redis、Mapper、Entity、Gateway 实现、MQ Producer、外部 SDK 等技术实现。
- `common`：配置、异常、工具、校验、拦截器等技术公共能力，不承载业务流程。

## 标准调用链

```text
Controller / RemoteServiceImpl / Job / MQ Consumer
    -> Manager
    -> DomainService / PersistencePort / Gateway / MqPort
    -> PersistencePortImpl / GatewayImpl / MqPortImpl
    -> Mapper / remote dubbo / HTTP / MQ
```

关键限制：

- `interfaces` 不直接调用 Mapper。
- `Controller` 不调用本服务自己的 `Remote*Service`。
- `RemoteServiceImpl` 不反向调用 Controller。
- `application` 不直接依赖 HTTP `CtrlParam`、HTTP `Vo`、RPC `Param`、RPC `Dto`。
- `Entity` 不返回到 `interfaces`。
- 跨表展示数据由 Manager 或 Assembler 通过多次单表查询后组装。

## 应用层对象

Manager 只接收和返回应用层对象：

- 写操作入参使用 `application/command/{context}` 下的 `*Command`。
- 查询入参使用 `application/query/{context}` 下的 `*Query`。
- 返回对象使用 `application/result/{context}` 下的 `*Result`。
- 复杂转换和组装可以放在 `application/assembler/{context}`。
