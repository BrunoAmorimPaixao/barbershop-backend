# Plano técnico em issues (curto, médio e longo prazo)

Este plano transforma melhorias do backend em **issues pequenas**, com escopo de **1–2 dias** cada.

## Como priorizar
- **Curto prazo (0–4 semanas):** segurança básica, estabilidade e previsibilidade de ambiente.
- **Médio prazo (1–2 meses):** regras de negócio críticas e qualidade de API.
- **Longo prazo (2+ meses):** robustez operacional, escala e governança.

---

## Curto prazo (0–4 semanas)

### ISSUE C1 — Migrar autenticação para JWT (base)
**Objetivo:** substituir token Base64 por autenticação segura.

**Tarefas (1–2 dias):**
1. Adicionar Spring Security e configuração mínima de filtro JWT.
2. Criar endpoint de login que emite JWT com expiração.
3. Bloquear endpoints `/api/**` por padrão e liberar apenas login.
4. Documentar variáveis de segredo/expiração no README.

**Critérios de aceite:**
- Login retorna JWT válido.
- Endpoints protegidos retornam 401 sem token.

---

### ISSUE C2 — Hash de senha e credenciais por ambiente
**Objetivo:** remover senha em texto puro e preparar base para múltiplos usuários.

**Tarefas (1–2 dias):**
1. Introduzir BCrypt para validação de senha.
2. Trocar `app.auth.password` por hash (`app.auth.password-hash`).
3. Criar script utilitário para gerar hash localmente.
4. Atualizar README com fluxo de configuração segura.

**Critérios de aceite:**
- Nenhuma comparação de senha em texto plano no código.
- Login funciona com hash configurado.

---

### ISSUE C3 — Flyway e baseline de schema
**Objetivo:** versionar banco e parar de depender de `ddl-auto=update` em produção.

**Tarefas (1–2 dias):**
1. Adicionar Flyway ao projeto.
2. Criar migration inicial com tabelas atuais.
3. Ajustar perfis: `dev` com conveniência e `prod` com `ddl-auto=validate`.
4. Documentar comandos e estratégia de migração.

**Critérios de aceite:**
- Aplicação sobe com migrations aplicadas.
- Produção não usa auto update de schema.

---

### ISSUE C4 — Seed controlado por perfil
**Objetivo:** impedir dados de demonstração em ambientes indevidos.

**Tarefas (1–2 dias):**
1. Colocar `DataInitializer` sob `@Profile("dev")`.
2. Migrar dados padrão para script de seed opcional.
3. Adicionar flag de enable/disable seed.
4. Documentar no README como popular ambiente local.

**Critérios de aceite:**
- Em produção, nenhum dado default é inserido automaticamente.

---

### ISSUE C5 — Padronização de erros da API
**Objetivo:** respostas de erro consistentes e rastreáveis.

**Tarefas (1–2 dias):**
1. Criar modelo único de erro (`timestamp`, `path`, `errorCode`, `message`, `details`).
2. Substituir uso de `IllegalArgumentException` por exceções de domínio.
3. Mapear status corretos (`400`, `404`, `409`, `422`).
4. Incluir `requestId/correlationId` no payload e logs.

**Critérios de aceite:**
- Todos erros seguem o mesmo contrato JSON.

---

## Médio prazo (1–2 meses)

### ISSUE M1 — Regra de conflito de agenda (overlap)
**Objetivo:** impedir dois agendamentos simultâneos para o mesmo barbeiro.

**Tarefas (1–2 dias):**
1. Definir política de conflito por janela de duração do serviço.
2. Implementar consulta de sobreposição no repositório.
3. Bloquear criação com retorno `409 Conflict`.
4. Cobrir cenários com testes unitários + integração.

**Critérios de aceite:**
- Não é possível reservar horário sobreposto para o mesmo barbeiro.

---

### ISSUE M2 — OpenAPI/Swagger com exemplos reais
**Objetivo:** facilitar consumo da API por frontend e QA.

**Tarefas (1–2 dias):**
1. Adicionar springdoc OpenAPI.
2. Documentar todos endpoints, inclusive `Idempotency-Key`.
3. Incluir exemplos de sucesso/erro nos schemas.
4. Publicar coleção Postman derivada da especificação.

**Critérios de aceite:**
- `/swagger-ui` disponível com cobertura completa dos endpoints.

---

### ISSUE M3 — Testes de integração HTTP
**Objetivo:** validar comportamento fim a fim da API.

**Tarefas (1–2 dias):**
1. Criar suíte com `@SpringBootTest` + banco de teste.
2. Cobrir auth, validação, criação de agendamento e idempotência.
3. Incluir caso concorrente (duas chamadas com mesma key).
4. Rodar testes no pipeline CI.

**Critérios de aceite:**
- Pipeline falha se contrato principal da API quebrar.

---

### ISSUE M4 — Hardening de idempotência
**Objetivo:** garantir replay previsível e auditável.

**Tarefas (1–2 dias):**
1. Persistir hash do payload associado à `Idempotency-Key`.
2. Rejeitar mesma chave com payload diferente (`409`).
3. Salvar metadados de resposta para replay consistente.
4. Definir política de retenção/expiração de chaves.

**Critérios de aceite:**
- Requisições repetidas têm comportamento determinístico.

---

### ISSUE M5 — Observabilidade base (Actuator + métricas)
**Objetivo:** melhorar diagnóstico de produção.

**Tarefas (1–2 dias):**
1. Adicionar Spring Boot Actuator.
2. Expor health/readiness/liveness com segurança.
3. Criar métricas de negócio (agendamentos criados, deduplicados, conflitos).
4. Padronizar logs estruturados com `requestId`.

**Critérios de aceite:**
- Health endpoints operacionais e métricas visíveis.

---

## Longo prazo (2+ meses)

### ISSUE L1 — RBAC completo (papéis e permissões)
**Objetivo:** separar responsabilidades operacionais.

**Tarefas (1–2 dias):**
1. Modelar papéis (`ADMIN`, `RECEPTION`, `BARBER`).
2. Aplicar autorização por endpoint e por ação.
3. Criar testes de autorização positivos/negativos.
4. Documentar matriz de permissões.

**Critérios de aceite:**
- Cada endpoint respeita política de acesso definida.

---

### ISSUE L2 — Resiliência da integração Google Calendar
**Objetivo:** reduzir impacto de falhas externas.

**Tarefas (1–2 dias):**
1. Adicionar timeout, retry com backoff e circuit breaker.
2. Criar fila/outbox para envio assíncrono de eventos.
3. Registrar status de sincronização por agendamento.
4. Criar job de reconciliação para reprocessar pendências.

**Critérios de aceite:**
- Falha externa não interrompe fluxo principal de agendamento.

---

### ISSUE L3 — Auditoria e trilha de mudanças
**Objetivo:** aumentar governança e suporte a compliance.

**Tarefas (1–2 dias):**
1. Introduzir auditoria (`createdBy`, `updatedBy`, timestamps).
2. Registrar mudanças críticas (agendamento criado/cancelado/reagendado).
3. Expor histórico para suporte administrativo.
4. Definir retenção de logs e política LGPD.

**Critérios de aceite:**
- Eventos críticos ficam rastreáveis por usuário e data.

---

### ISSUE L4 — Performance de listagens e paginação
**Objetivo:** manter API estável com crescimento de dados.

**Tarefas (1–2 dias):**
1. Adicionar paginação e filtros em listagens principais.
2. Revisar consultas com `EntityGraph`/projeções para reduzir N+1.
3. Criar índices para campos de busca frequentes.
4. Medir latência antes/depois com benchmark simples.

**Critérios de aceite:**
- Endpoints de listagem respondem com paginação consistente e desempenho previsível.

---

### ISSUE L5 — CI/CD com quality gates
**Objetivo:** elevar padrão de entrega contínua.

**Tarefas (1–2 dias):**
1. Configurar pipeline com build, testes e análise estática.
2. Definir coverage mínima para módulos críticos.
3. Bloquear merge em caso de falha de segurança/testes.
4. Publicar artefato versionado e changelog automático.

**Critérios de aceite:**
- Nenhum merge em branch principal sem quality gates aprovados.

---

## Backlog transversal (aplicar em todas as issues)
- Sempre incluir testes automatizados no escopo.
- Incluir atualização de documentação técnica/README.
- Definir impacto em rollout e plano de rollback quando necessário.
