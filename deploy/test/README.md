# 测试环境 Docker 部署

本目录用于部署 `rf-mng` 管理端测试环境，包含后端 Spring Boot 服务和前端 Nginx 静态服务。

## 前置条件

测试机需要安装：

- Docker
- Docker Compose v2
- Git

数据库拆分为平台主库 `rf_pt` 和机器人协作库 `rf_robot`。首次部署前需要确认已经执行：

1. 在 `rf_robot` 执行 qy_robot 税务机器人表结构，例如 `qy_robot/sql/010_add_tax_browser_worker.sql`、`qy_robot/sql/014_add_tax_social_security_payment.sql`、`qy_robot/sql/015_add_tax_bms_feedback.sql`。
2. 在 `rf_pt` 执行平台基础与业务表结构，例如 `backend/services/rf-mng/sql/rf_pt/20260622_platform_admin.sql`、`backend/services/rf-mng/sql/rf_pt/20260615_social_security_payment_management.sql`。

## 配置环境变量

```bash
cd /path/to/rf
cp deploy/test/.env.example deploy/test/.env
vi deploy/test/.env
```

至少需要确认：

- `RF_PLATFORM_DB_URL`
- `RF_PLATFORM_DB_USERNAME`
- `RF_PLATFORM_DB_PASSWORD`
- `RF_ROBOT_DB_URL`
- `RF_ROBOT_DB_USERNAME`
- `RF_ROBOT_DB_PASSWORD`
- `RF_TAX_ROBOT_BASE_URL`

`RF_TAX_ROBOT_BASE_URL` 指向 `tax-browser-worker`，例如 `http://192.168.110.192:3220`。

## 启动

```bash
bash deploy/test/deploy.sh
```

启动后访问：

```text
http://测试机IP:18092
```

后端接口端口默认暴露为：

```text
http://测试机IP:18091
```

## 查看日志

查看全部服务：

```bash
bash deploy/test/logs.sh
```

只看后端：

```bash
bash deploy/test/logs.sh rf-mng
```

只看前端 Nginx：

```bash
bash deploy/test/logs.sh rf-mng-web
```

## 停止

```bash
bash deploy/test/stop.sh
```

## 手动命令

```bash
cd deploy/test
docker compose --env-file .env build
docker compose --env-file .env up -d
docker compose --env-file .env ps
```
