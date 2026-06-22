# 后端文档索引

本目录存放 `rf` 后端开发规范和模块说明。编码助手处理后端任务时，先读取仓库根目录 `AGENTS.md`，再读取 `backend/docs/backend-rules/ASSISTANT.md`。

## 规范入口

- `backend-rules/ASSISTANT.md`：后端任务入口和专题路由。
- `backend-rules/core-ddd.md`：模块边界和分层职责。
- `backend-rules/api-contract.md`：HTTP、Dubbo、Job 等入口契约。
- `backend-rules/persistence.md`：Mapper、SQL、PersistencePort、时间字段规则。
- `backend-rules/naming-and-quality.md`：对象命名、注释、依赖注入、事务和基础质量。
- `backend-rules/checklist.md`：完成前自检清单。

## 模块说明

新增重要业务能力时，在本目录下按 Gradle 模块路径补充说明文档，例如：

```text
backend/docs/services/rf-performance/employee-performance.md
```

文档标题应包含模块名和主要代码路径，便于后续维护时快速定位。
