#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-barbershop-postgres}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker nao encontrado no PATH" >&2
  exit 1
fi

if docker compose version >/dev/null 2>&1; then
  DOCKER_COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  DOCKER_COMPOSE_CMD=(docker-compose)
else
  echo "docker compose ou docker-compose nao encontrado no PATH" >&2
  exit 1
fi

(
  cd "${ROOT_DIR}"
  "${DOCKER_COMPOSE_CMD[@]}" -f docker-compose.observability.yml down
)

if docker ps -a --format '{{.Names}}' | grep -Fxq "${POSTGRES_CONTAINER}"; then
  docker stop "${POSTGRES_CONTAINER}" >/dev/null || true
  docker rm "${POSTGRES_CONTAINER}" >/dev/null || true
fi

echo "Stack local encerrada."
