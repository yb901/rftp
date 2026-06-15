# 测试环境 Docker 部署

本目录用于部署 `rfpt-mng` 管理端测试环境，包含后端 Spring Boot 服务和前端 Nginx 静态服务。

## 前置条件

测试机需要安装：

- Docker
- Docker Compose v2
- Git

数据库使用现有 `rf_tax`。首次部署前需要确认已经执行：

1. `qy_robot/sql/014_add_tax_social_security_payment.sql`
2. `backend/services/rfpt-mng/sql/20260615_social_security_payment_management.sql`

## 配置环境变量

```bash
cd /path/to/rfpt
cp deploy/test/.env.example deploy/test/.env
vi deploy/test/.env
```

至少需要确认：

- `RFPT_DB_URL`
- `RFPT_DB_USERNAME`
- `RFPT_DB_PASSWORD`
- `RFPT_TAX_ROBOT_BASE_URL`

`RFPT_TAX_ROBOT_BASE_URL` 指向 `tax-browser-worker`，例如 `http://192.168.110.192:3220`。

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
bash deploy/test/logs.sh rfpt-mng
```

只看前端 Nginx：

```bash
bash deploy/test/logs.sh rfpt-mng-web
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
