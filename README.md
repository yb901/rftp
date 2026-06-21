# rfpt

`rfpt` 是社保缴费业务的管理系统仓库，前后端放在同一个 Git 仓库中。

## 模块边界

- `backend/services/rfpt-mng`：管理端后端，负责企业、地区配置、社保缴费批次、任务状态、失败原因和重试管理。
- `frontend/rfpt-mng-node`：管理端前端，负责运营人员发起批次、查看进度和处理失败任务。
- `qy_robot/tax-browser-worker`：浏览器自动化执行端，继续作为独立机器人项目存在，管理端通过内部接口调用它。

## 第一阶段范围

第一阶段围绕“社保缴费”流程沉淀管理能力：

1. 维护需要缴费的企业和地区站点配置。
2. 按地区和月份发起缴费批次。
3. 为批次生成企业维度任务。
4. 调用机器人执行电子税务局流程。
5. 记录 WPM 明细校验、缴费、完税凭证开具/下载等过程数据。
6. 支持失败任务查看原因和重试。

## 本地开发

后端：

```bash
cd backend
./gradlew :services:rfpt-mng:rfpt-mng-provider:compileJava
```

前端：

```bash
cd frontend/rfpt-mng-node
npm install
npm run dev
```

数据库暂时复用 `rf_tax`，连接信息通过环境变量或配置中心注入，不在源码中保存密码。

## 后端 SQL 约束

- Mapper XML 和注解 SQL 只写简单、直接的单表 SQL。
- 禁止使用 `JOIN` / 联表查询。
- 需要跨表展示的数据，先分别按索引条件批量查询，再在 Java Manager / Assembler 层组装。
- 社保缴费模块也遵循该规则，例如批次地区名称、任务企业名称等展示字段均通过 Java 层回填。


## 测试环境 Docker 部署

测试环境部署脚本位于 `deploy/test`，包含后端、前端镜像构建和 `docker compose` 编排。

首次部署：

```bash
git clone git@codeup.aliyun.com:6a0e7b2c7b6e0a0129639206/rfpt/rfpt.git
cd rfpt
cp deploy/test/.env.example deploy/test/.env
vi deploy/test/.env
bash deploy/test/deploy.sh
```

启动后访问：

```text
http://测试机IP:18092
```

后端接口默认端口：

```text
http://测试机IP:18091
```

详细说明见 `deploy/test/README.md`。
