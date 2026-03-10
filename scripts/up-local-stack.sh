#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-barbershop-postgres}"
POSTGRES_DB="${POSTGRES_DB:-barbershop}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-postgres}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
BACKEND_PORT="${BACKEND_PORT:-8080}"

GRAFANA_ADMIN_USER="${GRAFANA_ADMIN_USER:-admin}"
GRAFANA_ADMIN_PASSWORD="${GRAFANA_ADMIN_PASSWORD:-admin-change-me}"
APP_CORS_ALLOWED_ORIGIN="${APP_CORS_ALLOWED_ORIGIN:-http://localhost:5173}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker nao encontrado no PATH" >&2
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "mvn nao encontrado no PATH" >&2
  exit 1
fi

if ! docker ps -a --format '{{.Names}}' | grep -Fxq "${POSTGRES_CONTAINER}"; then
  docker run -d \
    --name "${POSTGRES_CONTAINER}" \
    -e POSTGRES_DB="${POSTGRES_DB}" \
    -e POSTGRES_USER="${POSTGRES_USER}" \
    -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
    -p "${POSTGRES_PORT}:5432" \
    postgres:16-alpine >/dev/null
elif [ "$(docker inspect -f '{{.State.Running}}' "${POSTGRES_CONTAINER}")" != "true" ]; then
  docker start "${POSTGRES_CONTAINER}" >/dev/null
fi

echo "Postgres em http://localhost:${POSTGRES_PORT}"

(
  cd "${ROOT_DIR}"
  GRAFANA_ADMIN_USER="${GRAFANA_ADMIN_USER}" \
  GRAFANA_ADMIN_PASSWORD="${GRAFANA_ADMIN_PASSWORD}" \
  docker compose -f docker-compose.observability.yml up -d
)

echo "Prometheus em http://localhost:9090"
echo "Grafana em http://localhost:3000"
echo "Subindo backend em http://localhost:${BACKEND_PORT}"

cd "${ROOT_DIR}"
SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}" \
SPRING_DATASOURCE_USERNAME="${POSTGRES_USER}" \
SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD}" \
APP_CORS_ALLOWED_ORIGIN="${APP_CORS_ALLOWED_ORIGIN}" \
mvn spring-boot:run
