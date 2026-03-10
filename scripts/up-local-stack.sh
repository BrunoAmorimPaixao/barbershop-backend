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

if command -v ss >/dev/null 2>&1; then
  PORT_IN_USE_CHECK_CMD=(ss -ltn)
elif command -v netstat >/dev/null 2>&1; then
  PORT_IN_USE_CHECK_CMD=(netstat -ltn)
else
  PORT_IN_USE_CHECK_CMD=()
fi

if docker compose version >/dev/null 2>&1; then
  DOCKER_COMPOSE_CMD=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  DOCKER_COMPOSE_CMD=(docker-compose)
else
  echo "docker compose ou docker-compose nao encontrado no PATH" >&2
  exit 1
fi

if [ "${#PORT_IN_USE_CHECK_CMD[@]}" -gt 0 ] && ! docker ps -a --format '{{.Names}}' | grep -Fxq "${POSTGRES_CONTAINER}"; then
  if "${PORT_IN_USE_CHECK_CMD[@]}" | grep -Eq "[\.\:]${POSTGRES_PORT}[[:space:]]"; then
    echo "a porta ${POSTGRES_PORT} ja esta em uso; use outra porta, por exemplo:" >&2
    echo "POSTGRES_PORT=5433 ./scripts/up-local-stack.sh" >&2
    exit 1
  fi
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

for attempt in $(seq 1 30); do
  if docker exec "${POSTGRES_CONTAINER}" psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -c 'select 1' >/dev/null 2>&1; then
    break
  fi

  if [ "${attempt}" -eq 30 ]; then
    echo "Postgres nao ficou pronto a tempo para conexoes SQL" >&2
    exit 1
  fi

  sleep 2
done

(
  cd "${ROOT_DIR}"
  GRAFANA_ADMIN_USER="${GRAFANA_ADMIN_USER}" \
  GRAFANA_ADMIN_PASSWORD="${GRAFANA_ADMIN_PASSWORD}" \
  "${DOCKER_COMPOSE_CMD[@]}" -f docker-compose.observability.yml up -d
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
