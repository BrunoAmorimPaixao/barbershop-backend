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
