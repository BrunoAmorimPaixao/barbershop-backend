# Barbershop Backend

API backend do sistema Barbershop usando Spring Boot.

## Como rodar localmente

Pré-requisitos:

- Java 21
- Maven 3.9+
- PostgreSQL em execução

Executar:

```bash
mvn spring-boot:run
```

A API sobe por padrão em `http://localhost:8080`.

Subir a stack local completa com um comando:

```bash
chmod +x scripts/up-local-stack.sh
./scripts/up-local-stack.sh
```

O script sobe:

- `PostgreSQL` em Docker
- `Prometheus` e `Grafana` via `docker compose`
- o backend com `mvn spring-boot:run`

Encerrar a stack local:

```bash
chmod +x scripts/down-local-stack.sh
./scripts/down-local-stack.sh
```

## Variáveis de ambiente

Configuração de banco de dados:

- `SPRING_DATASOURCE_URL` (ex.: `jdbc:postgresql://localhost:5432/barbershop`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Configuração de CORS:

- `APP_CORS_ALLOWED_ORIGIN` (ex.: `http://localhost:5173`)

Exemplo:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/barbershop
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export APP_CORS_ALLOWED_ORIGIN=http://localhost:5173
mvn spring-boot:run
```

## Observabilidade

O backend expõe endpoints do Spring Boot Actuator para saúde e métricas Prometheus:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/prometheus`

Stack mínima recomendada:

- `Prometheus` para coletar métricas do endpoint `/actuator/prometheus`
- `Grafana` para dashboards e alertas
- `Loki` para logs centralizados quando quiser sair de `kubectl logs`

No Kubernetes, o manifesto `k8s/03-backend.yaml` já fica preparado com:

- annotations de scrape para Prometheus
- `readinessProbe` e `livenessProbe` usando os endpoints do Actuator

Para ambiente produtivo básico, o repositório também inclui:

- `k8s/04-prometheus.yaml` com `ConfigMap`, `PVC`, `Deployment` e `Service`
- `k8s/05-grafana.yaml` com datasource provisionado para Prometheus, `PVC`, `Deployment` e `Service`
- `k8s/06-cert-manager-clusterissuer.yaml` para emissao automatica de certificado TLS com `cert-manager`
- `k8s/07-grafana-ingress.yaml` para expor o Grafana com `Ingress` HTTPS

O Grafana tambem sobe com dashboard inicial provisionado automaticamente:

- `Barbershop Overview`
- throughput HTTP
- taxa de erro `5xx`
- latencia media e por rota
- memoria JVM
- conexoes do `HikariCP`

Métricas iniciais que valem dashboard/alerta:

- latência HTTP por rota
- taxa de erro HTTP `5xx`
- volume de requisições
- uso de memória JVM
- uso de CPU do processo
- conexões e tempo de resposta do banco

### Deploy da stack

Aplicar os manifests nesta ordem:

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secret.yaml
kubectl apply -f k8s/02-postgres.yaml
kubectl apply -f k8s/03-backend.yaml
kubectl apply -f k8s/04-prometheus.yaml
kubectl apply -f k8s/05-grafana.yaml
kubectl apply -f k8s/06-cert-manager-clusterissuer.yaml
kubectl apply -f k8s/07-grafana-ingress.yaml
```

Ou com um comando so via script:

```bash
chmod +x scripts/deploy-k8s-stack.sh
./scripts/deploy-k8s-stack.sh
```

Para incluir `Ingress` e TLS do Grafana no deploy:

```bash
INCLUDE_INGRESS=true ./scripts/deploy-k8s-stack.sh
```

### Backend no Minikube

Para cluster local com `minikube`, o backend usa `imagePullPolicy: IfNotPresent` em [k8s/03-backend.yaml](/home/brunopaixao/IdeaProjects/barbershop-backend/k8s/03-backend.yaml). Antes do deploy, faca o build e carregue a imagem no cluster:

```bash
chmod +x scripts/build-and-load-minikube-image.sh
./scripts/build-and-load-minikube-image.sh
```

Depois aplique a stack:

```bash
./scripts/deploy-k8s-stack.sh
```

Credenciais iniciais do Grafana:

- usuário: valor de `GRAFANA_ADMIN_USER`
- senha: valor de `GRAFANA_ADMIN_PASSWORD`

Antes de produção real, troque obrigatoriamente `GRAFANA_ADMIN_PASSWORD` em `k8s/01-secret.yaml`.

### Ingress do Grafana

Os arquivos `k8s/06-cert-manager-clusterissuer.yaml` e `k8s/07-grafana-ingress.yaml` assumem:

- `ingress-nginx` instalado no cluster
- `cert-manager` instalado no cluster
- DNS publico apontando para o controlador de ingress

Antes de aplicar, ajuste:

- `ops@example.com` em `k8s/06-cert-manager-clusterissuer.yaml`
- `grafana.example.com` em `k8s/07-grafana-ingress.yaml`

Essa versao usa TLS com `Let's Encrypt` e login local do Grafana. Para producao mais madura, o ideal e trocar o login local por `OIDC` com Google, GitHub, Azure AD ou outro provedor.

## Observabilidade Local com Docker

Para desenvolvimento, voce pode subir `Prometheus` e `Grafana` fora do cluster:

```bash
docker compose -f docker-compose.observability.yml up -d
```

Isso sobe:

- Grafana em `http://localhost:3000`
- Prometheus em `http://localhost:9090`

O Prometheus local coleta do backend em `http://host.docker.internal:8080/actuator/prometheus`, entao o backend precisa estar rodando localmente na porta `8080`.

Credenciais padrao do Grafana no Docker:

- usuario: `admin`
- senha: `admin-change-me`

Voce pode sobrescrever com variaveis de ambiente:

```bash
export GRAFANA_ADMIN_USER=admin
export GRAFANA_ADMIN_PASSWORD='uma-senha-forte'
docker compose -f docker-compose.observability.yml up -d
```

### Limites desta configuração

Essa base é adequada para produção simples, mas ainda não cobre alta disponibilidade. Para endurecer de verdade, o próximo passo é adicionar:

- storage class explícita para os `PVCs`
- backup dos volumes do Prometheus e Grafana
- alertas no Prometheus ou Grafana
- centralização de logs com `Loki`
- `OIDC` no Grafana para substituir login local

## Docker

O `Dockerfile` deste repositório empacota e executa o backend a partir do JAR gerado.

## Deploy no Kubernetes

Atualizar a versao da imagem do backend no cluster:

```bash
./scripts/deploy-backend.sh v1.0.3
```

O script executa:

- `kubectl -n barbershop set image deployment/backend backend=barbershop-backend:<tag>`
- `kubectl -n barbershop rollout status deployment/backend`

Manifesto base:

- `k8s/03-backend.yaml` usa uma tag fixa de release (`barbershop-backend:v1.0.0`).
- Atualize a tag no manifesto sempre que publicar uma nova versao.

Depois, para acompanhar no k9s:

```bash
k9s -n barbershop
```
