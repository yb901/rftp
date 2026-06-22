# rf

`rf` 是人力业务相关系统仓库，当前包含社保缴费管理和员工绩效查看/确认反馈能力。仓库采用前后端同库管理，后端参考 `zy_qy` 的 DDD 分层规范，前端包含管理后台和公众号 H5。

## 技术栈

- 后端：JDK 21、Spring Boot 3、Dubbo 3、Nacos 2、MySQL 8、Redis、XXL-JOB、Gradle 8.8。
- 管理后台：React 18、Ant Design 5、Vite、TypeScript。
- 员工端 H5：React 18、Ant Design Mobile 5、Vite、TypeScript。
- 测试部署：Docker Compose。
- 生产部署：K8s + 云效流水线。

## 目录结构

```text
backend/
  common/                         后端公共能力
  services/
    rf-mng/                     管理端后端
    rf-performance/             员工绩效后端
  docs/backend-rules/             后端开发规范
  k8s/prod/                       生产后端 K8s 配置
frontend/
  rf-mng-node/                  管理后台前端
  rf-h5-node/                   员工端公众号 H5
deploy/
  test/                           测试环境 Docker Compose
  prod/                           生产云效流水线说明
```

## 业务模块

- 社保缴费：企业和地区配置、缴费批次、企业维度任务、过程状态、失败原因和重试。
- 员工绩效：管理员创建绩效任务、导入员工绩效、员工手机号验证登录、查看绩效、确认或反馈、自动确认、后台调整、反馈和调整留痕、查询与导出。

## 开发规范入口

执行具体开发前先读取根目录 `AGENTS.md`，再按任务范围读取：

- 后端：`backend/docs/backend-rules/ASSISTANT.md`
- 管理后台：`frontend/rf-mng-node/docs/frontend-rules/ASSISTANT.md`
- H5：`frontend/rf-h5-node/docs/frontend-rules/ASSISTANT.md`

核心约束摘要：

- 后端采用 `api` / `provider` 双模块和 `interfaces`、`application`、`domain`、`infrastructure`、`common` 分层。
- Controller、RemoteServiceImpl、Job 只做入口适配，业务编排放 Manager。
- 单表持久化通过 PersistencePort 隔离。
- Mapper XML 和注解 SQL 禁止 `JOIN` / 联表查询，跨表展示在 Java 层批量查询后组装。
- 数据库自动维护的创建/更新时间字段不在业务 SQL 中手动赋值。

## 本地验证

后端：

```bash
cd backend
./gradlew :services:rf-mng:rf-mng-provider:compileJava
./gradlew :services:rf-performance:rf-performance-provider:compileJava
```

管理后台：

```bash
cd frontend/rf-mng-node
npm install
npm run build
```

员工端 H5：

```bash
cd frontend/rf-h5-node
npm install
npm run build
```

数据库拆分为平台主库 `rf_pt` 和机器人协作库 `rf_robot`。平台业务表放在 `rf_pt`，tax-browser-worker 与管理端交互的税务机器人表放在 `rf_robot`，连接信息通过环境变量或配置中心注入，不在源码中保存密码。

## 部署

- 测试环境部署脚本位于 `deploy/test`，使用 Docker Compose 编排。
- 生产环境部署说明位于 `deploy/prod`，后端和前端 K8s 配置分别位于 `backend/k8s/prod`、`frontend/*/k8s/prod`。
