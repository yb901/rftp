#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

if [[ ! -f .env ]]; then
  echo "[ERROR] 缺少 deploy/test/.env，请先复制 .env.example 并填写数据库密码等参数。" >&2
  echo "        cp deploy/test/.env.example deploy/test/.env" >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] 未找到 docker 命令。" >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] 当前 Docker 不支持 docker compose。" >&2
  exit 1
fi

echo "[INFO] 构建测试环境镜像"
docker compose --env-file .env build

echo "[INFO] 启动测试环境服务"
docker compose --env-file .env up -d

echo "[INFO] 当前服务状态"
docker compose --env-file .env ps

echo "[INFO] 部署完成。前端入口：http://<测试机IP>:${RF_MNG_WEB_PORT:-18092}"
