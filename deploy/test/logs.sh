#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

ENV_FILE=.env
if [[ ! -f "${ENV_FILE}" ]]; then
  ENV_FILE=.env.example
fi

SERVICE="${1:-}"
if [[ -n "${SERVICE}" ]]; then
  docker compose --env-file "${ENV_FILE}" logs -f --tail=200 "${SERVICE}"
else
  docker compose --env-file "${ENV_FILE}" logs -f --tail=200
fi
