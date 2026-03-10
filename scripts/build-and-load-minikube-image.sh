#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE_NAME="${IMAGE_NAME:-barbershop-backend}"
IMAGE_TAG="${IMAGE_TAG:-v1.0.0}"
FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker nao encontrado no PATH" >&2
  exit 1
fi

if ! command -v minikube >/dev/null 2>&1; then
  echo "minikube nao encontrado no PATH" >&2
  exit 1
fi

cd "${ROOT_DIR}"

echo "Buildando imagem ${FULL_IMAGE}"
docker build -t "${FULL_IMAGE}" .

echo "Carregando imagem ${FULL_IMAGE} no minikube"
minikube image load "${FULL_IMAGE}"

echo "Imagem pronta para uso no cluster local."
