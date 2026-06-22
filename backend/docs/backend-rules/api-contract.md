# API 与入口契约规则

## HTTP Controller

Controller、HTTP 入参和返回对象按业务上下文就近放置：

```text
interfaces/{context}/*Controller.java
interfaces/{context}/param/*CtrlParam.java
interfaces/{context}/vo/*Vo.java
interfaces/{context}/converter/*Converter.java
```

已有代码若使用 `interfaces/controller/{context}`，继续沿用当前模块局部模式，不为了路径统一做无意义搬迁。

Controller 只做协议适配、参数转换、基础校验和统一响应包装。复杂业务流程、跨表编排、外部系统调用和事务边界放到应用层。

## Dubbo API

`api` 模块只放对外契约：

```text
api/remoteservice/{context}
api/param/{context}
api/query/{context}
api/dto/{context}
api/enums/{context}
```

`api` 禁止放 provider 内部 `Entity`、`Vo`、`Manager`、`Service`、`Mapper`、领域模型和业务流程逻辑。

RPC 请求入参统一使用 `*Param` 或 `*Query`，RPC 返回对象统一使用 `*Dto`。HTTP 入参和 RPC 入参即使字段相近，也保持独立类，通过 converter 或应用层组装转换。

## RemoteServiceImpl

Dubbo 实现放在：

```text
interfaces/remoteserviceimpl/{context}
interfaces/remoteserviceimpl/{context}/converter
```

RemoteServiceImpl 是入口适配器，职责与 Controller 类似：完成 RPC 参数转换、基础校验、调用 Manager、转换返回对象。RemoteServiceImpl 不反向调用 Controller。

## Job、Callback、MQ Consumer

入口适配器按来源放置：

```text
interfaces/job/{context}
interfaces/callback/{context}
interfaces/mq/consumer/{context}
```

Job、Callback、MQ Consumer 只做协议适配、参数转换、基础校验和幂等入口控制，然后调用应用层。XXL-JOB 定时任务实现中不要直接写复杂业务流程。

## Gateway

下游 Dubbo、HTTP、第三方系统包装统一命名为 `Gateway`，不要命名为 `Service`。

- 应用层接口放在 `application/port/gateway/{context}/{capability}`。
- 基础设施实现放在 `infrastructure/port/gateway/internal/{system}/{context}/{capability}` 或 `infrastructure/port/gateway/external/{context}/{capability}`。
- 内部系统如机器人、其他后端服务走 `internal`；公网第三方如短信、图形验证码走 `external`。

Gateway 入参命名为 `*GatewayParam`，返回对象命名为 `*GatewayResult`。Gateway 只做技术协议适配、下游对象转换和错误归一，不承载用例编排。
