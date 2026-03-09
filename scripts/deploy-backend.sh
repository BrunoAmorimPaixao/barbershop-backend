#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Uso: $0 <tag-da-imagem>"
  echo "Exemplo: $0 v1.0.3"
  exit 1
fi

TAG="$1"
IMAGE="barbershop-backend:${TAG}"
NAMESPACE="barbershop"
DEPLOYMENT="backend"
CONTAINER="backend"

echo "Atualizando deployment ${DEPLOYMENT} com imagem ${IMAGE}..."
kubectl -n "${NAMESPACE}" set image "deployment/${DEPLOYMENT}" "${CONTAINER}=${IMAGE}"

echo "Aguardando rollout..."
kubectl -n "${NAMESPACE}" rollout status "deployment/${DEPLOYMENT}"

echo "Deploy concluido."
