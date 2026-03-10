#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INCLUDE_INGRESS="${INCLUDE_INGRESS:-false}"

if ! command -v kubectl >/dev/null 2>&1; then
  echo "kubectl nao encontrado no PATH" >&2
  exit 1
fi

MANIFESTS=(
  "k8s/00-namespace.yaml"
  "k8s/01-secret.yaml"
  "k8s/02-postgres.yaml"
  "k8s/03-backend.yaml"
  "k8s/04-prometheus.yaml"
  "k8s/05-grafana.yaml"
)

if [ "${INCLUDE_INGRESS}" = "true" ]; then
  MANIFESTS+=(
    "k8s/06-cert-manager-clusterissuer.yaml"
    "k8s/07-grafana-ingress.yaml"
  )
fi

cd "${ROOT_DIR}"

for manifest in "${MANIFESTS[@]}"; do
  echo "Aplicando ${manifest}"
  kubectl apply -f "${manifest}"
done

echo "Stack aplicada no namespace barbershop."
echo "Acompanhe com: k9s -n barbershop"
