# services 目录说明

`backend/services` 存放 `rf` 后端业务服务模块。新增或修改服务代码前，先读取：

```text
backend/docs/backend-rules/ASSISTANT.md
```

## 当前服务

- `rf-mng`：管理端服务，承载管理后台 API、社保缴费管理、员工绩效管理入口。
- `rf-performance`：员工绩效服务，承载绩效任务、员工绩效记录、H5 登录、确认、反馈、自动确认和后台调整能力。

## 模块约束

- 新业务服务优先采用 `api` / `provider` 双模块。
- `api` 只放对外 RPC 契约。
- `provider` 顶层只放 `interfaces`、`application`、`domain`、`infrastructure`、`common`。
- SQL 保持单表、简单、直接，禁止在 Mapper 中写联表查询。
