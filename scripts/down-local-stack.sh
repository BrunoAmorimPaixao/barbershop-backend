#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-barbershop-postgres}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker nao encontrado no PATH" >&2
  exit 1
fi

(
  cd "${ROOT_DIR}"
  docker compose -f docker-compose.observability.yml down
)

if docker ps -a --format '{{.Names}}' | grep -Fxq "${POSTGRES_CONTAINER}"; then
  docker stop "${POSTGRES_CONTAINER}" >/dev/null || true
  docker rm "${POSTGRES_CONTAINER}" >/dev/null || true
fi

echo "Stack local encerrada."
