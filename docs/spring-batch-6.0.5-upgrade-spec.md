# Spec — Upgrade para Spring Batch 6.0.5

**Data:** 2026-09-14
**Autor:** Claude Code + Frederico G. Costa
**Status:** concluído (branch `feat/spring-batch-6.0.5-upgrade`) — Fases 0-2 e 4 aplicadas e testadas; Fase 3 (docker/SCDF/OTel) aplicada como spike (config validada estruturalmente, stack completa não subida ponta a ponta)

## 1. Contexto

Projeto Spring Boot **2.2.6.RELEASE** / Java 11 / Spring Batch 4.x / Spring Cloud Hoxton.SR4 que lê arquivo fixo de transações, divide cada registro multi-linha em entidades JPA e persiste no PostgreSQL. Também é empacotado como app Spring Cloud Task para rodar sob Spring Cloud Data Flow (SCDF) via docker-compose.

Objetivo: **atualizar para Spring Batch 6.0.5**, usando **OpenRewrite** para a migração programática, e migrar as métricas do proxy RSocket legado para **OpenTelemetry (OTLP)**.

## 2. Decisões de versão

| Componente | Atual | Alvo |
|---|---|---|
| Spring Boot (parent) | 2.2.6.RELEASE | **4.0.8** |
| Spring Framework | 5.2.x | **7.0.9** |
| Spring Batch | 4.1.x | **6.0.5** (pin explícito `spring-batch.version`) |
| Spring Cloud (BOM) | Hoxton.SR4 | **2025.1.3** (resolvido pelo recipe; estimativa original era 2025.0.x) (task 3.3.x) |
| Java | 11 | **17** (toolchain local: JDK 21) |
| logstash-logback-encoder | 6.3 | **8.1** |
| micrometer-jvm-extras | 0.2.0 | **remover** (unmaintained) |
| assertj-core | 3.15.0 (fixo) | gerenciado pelo BOM do Boot |

**Por que Boot 4.0 e não 3.2?** Verificado nos POMs do Maven Central: Spring Batch 6.0.5 depende de Spring Framework 7.0.9, que só existe no Boot 4.0.x. Boot 3.2.x só vai até Batch 5.1.x. Caminho escolhido pelo usuário: **Boot 4.0.8**.

## 3. Descobertas-chave (verificadas nos jars do Maven Central)

### 3.1 O Spring Batch 6.0.5 é uma reorganização de pacotes, não só bump de versão

Novo módulo `spring-batch-infrastructure` foi criado:

- `org.springframework.batch.item.*` → `org.springframework.batch.infrastructure.item.*` (jar core 6.0.5 tem 0 classes `item`; infra tem 360)
- `org.springframework.batch.repeat.*` → `org.springframework.batch.infrastructure.repeat.*`
- `core.Job` → `core.job.Job`, `core.Step` → `core.step.Step`, `core.StepContribution` → `core.step.StepContribution`
- `core.launch.support.RunIdIncrementer` → `core.job.parameters.RunIdIncrementer`
- `EnableBatchProcessing`, `@StepScope` e `JobBuilderFactory`/`StepBuilderFactory` (Boot) mudam de pacote em menor medida — o recipe trata.

**Impacto:** praticamente todos os arquivos de `steps/` e o `BatchConfiguration` importam pacotes que se moveram.

### 3.2 OpenRewrite cobre a migração com UM recipe

`org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0` (em `rewrite-spring:6.37.1`) é **auto-cadeado**: encadeia `UpgradeSpringBoot_3_5` → … → `3_0` → `2_7` → … → `2_0` e já inclui internamente:

- `SpringBatch5To6Migration` (lista de `ChangeType`/`ChangePackage` bate exatamente com o layout do jar 6.0.5)
- `UpgradeSpringCloud_2025`
- `UpgradeSpringFramework_7_0`, `MigrateToHibernate71`, `UpgradeToJava17`
- migração javax→jakarta via `JavaxMigrationToJakarta` **curada** (regras por spec; **não** mexe em `javax.sql` — o `javax.sql.DataSource` nos testes fica intacto)
- `MigrateToModularStarters` + `RemoveEnableBatchProcessing` (remover o `@EnableBatchProcessing` — obsoleto no Boot 3+)

O que o OpenRewrite **NÃO** faz (trabalho manual na Fase 2): métricas RSocket→OTel, fix do wiring `@StepScope` no `BatchConfiguration`, docker/SCDF/README, bump do logstash-logback-encoder, remoção do micrometer-jvm-extras.

### 3.3 Ambiente / tooling

- `maven-wrapper.jar` do repositório está com **0 bytes** (corrompido no commit); `./mvnw` falha com `ClassNotFoundException: MavenWrapperMain`. Mitigação de curto prazo: usar `mvn` do sistema (`/usr/share/maven/bin/mvn`, 3.6.3). Reparado na **Fase 0** (ver seção 5), antes de iniciar a migração.
- `rewrite-maven-plugin`: pedido foi usar **6.48.0** (última release no GitHub), mas ela **ainda não sincronizou para o Maven Central** (404; repo spring-milestones bloqueado pelo proxy com 403). Fallback confirmado-resolúvel: **6.46.1** (mais alta no Central). Reavaliar trocar para 6.48.0+ quando sincronizar.
- Rede: proxy corporativo (`host.containers.internal:3128`); Maven Central acessível.

### 3.4 Bugs deixados pelo recipe (descobertos só na execução, corrigidos manualmente)

Além do já esperado (métricas, docker, README), o recipe deixou o build **quebrado** em 3 pontos não previstos no plano original — nenhum é opcional, todos bloqueavam `mvn compile`:

1. **`BatchConfiguration.souJavaJob()`** chamava `taskletStep()`/`chunkletStep()` sem argumentos, mas o próprio recipe mudou as assinaturas desses métodos para exigir `JobRepository`/`PlatformTransactionManager` (API nova do `StepBuilder`/`JobBuilder` do Batch 6). Corrigido passando os parâmetros adiante.
2. **`PatternMatchingCompositeLineMapper`** perdeu o construtor sem argumentos no Batch 6 (`DefaultCompositeLineMapper` chamava `setTokenizers`/`setFieldSetMappers` depois de um `super()` implícito). Corrigido construindo os `Map`s antes e passando via `super(tokenizers, fieldSetMappers)`.
3. **Ambiguidade de `TransactionManager`** em runtime (não em compile-time): `spring-cloud-starter-task` registra `springCloudTaskTransactionManager` além do `transactionManager` da JPA, então o `@Transactional` sem qualificador em `TransactionItemWriter.write()` lançava `NoUniqueBeanDefinitionException` só quando o step realmente rodava (o `souJavaJob` completava com status `FAILED`, mas os testes JUnit não falhavam porque não fazem assert do status do job). Corrigido trocando para `org.springframework.transaction.annotation.Transactional("transactionManager")` — a anotação `jakarta.transaction.Transactional` que estava em uso não suporta qualificar o bean por nome.

**Nota:** este último item confirma, na prática, que o `reader(null, null)` mencionado na Fase 2 do plano **de fato não precisou de nenhuma mudança** — permaneceu intocado no diff do recipe e compilou/rodou normalmente.

## 4. Escopo

**Incluído:**
1. App + testes (Java, pom, propriedades)
2. Stack docker/SCDF/Prometheus→OTel/Grafana
3. Métricas: remover `prometheus-rsocket-*` + `micrometer-registry-prometheus`; adicionar OTel
4. README (versões, comandos, tabela de tools)

**Fora de escopo:** `application.yml.bkp` (backup morto — decidir: atualizar ou excluir), dashboards Grafana (verificar labels só se os nomes de métrica mudarem), configs `.vscode/`.

## 5. Fases de implementação

### Fase 0 — Preparação de tooling

- Reparar `maven-wrapper.jar` (0 bytes) **antes** de iniciar a migração, e não na Fase 4. Rodar tudo com `mvn` do sistema (3.6.3) até então é frágil para validar um build Boot 4/Java 17 — reduz a confiança de que o build final é reprodutível. Regenerar com `mvn wrapper:wrapper -Dmaven=3.9.x` (ou baixar o jar oficial) e confirmar `./mvnw -v` funciona antes da Fase 1.

### Fase 1 — OpenRewrite (código + POM)

1. Plugin `rewrite-maven-plugin:6.46.1` no `<build><plugins>` do pom com `activeRecipes = [UpgradeSpringBoot_4_0]` e `recipeArtifactCoordinates` (`rewrite-spring:6.37.1`, `rewrite-migrate-java:3.42.1`) — **já adicionado**.
2. `mvn org.openrewrite.maven:rewrite-maven-plugin:6.46.1:dryRun` → revisar `git diff` **antes** de aplicar.
3. `mvn …:run` → rever diff novamente.
   - Se a cadeia 2.2.6→4.0.8 num `run` só deixar estado inconsistente, rodar os recipes `UpgradeSpringBoot_*` sequencialmente (um `run` por versão), revisando entre passes.
4. **Checkpoint de compilação:** rodar `mvn compile` (não só o diff) logo após o `run`, antes de avançar para a Fase 2 — pega erros de import/pacote cedo, dado que o salto de pacotes (`item` → `infrastructure.item`, `core.Job`→`core.job.Job`, etc.) é extenso.

### Fase 2 — Retoque manual

**`pom.xml`:**
- Pinar `spring-batch.version=6.0.5` (o recipe deixa `6.0.x`)
- `logstash-logback-encoder` 6.3 → 8.1
- Remover `micrometer-jvm-extras`
- Métricas: remover `prometheus-rsocket-spring`, `prometheus-rsocket-client`, `micrometer-registry-prometheus`; adicionar (sem versão, gerenciados pelo BOM): `io.micrometer:micrometer-registry-otlp`, `io.micrometer:micrometer-tracing-bridge-otel` (traz o SDK OTel transitivamente), `io.opentelemetry:opentelemetry-exporter-otlp`
- `assertj-core`: remover versão explícita
- Decidir: manter ou remover o plugin OpenRewrite do pom ao final
- **Verificação pós-limpeza:** rodar `mvn dependency:tree | grep -i prometheus` (ou `rsocket`) para confirmar que nenhum artefato `io.micrometer.prometheus:*`/`prometheus-rsocket-*` sobra transitivamente (ex.: via `spring-cloud-starter-task`/actuator) antes de marcar o critério de aceite "nenhuma referência RSocket" como atendido.

**`BatchConfiguration`:**
- Não re-adicionar `@EnableBatchProcessing` (o recipe remove)
- **Sobre o `reader(null, null)` em `chunkletStep()`:** **não é um bug e não precisa de fix funcional.** O `CLAUDE.md` já documenta corretamente que isso funciona porque `@Configuration` usa proxy CGLIB (`proxyBeanMethods=true`, default inalterado no Boot 4/Batch 6): a chamada intra-classe é interceptada e retorna o bean `@StepScope` real, os `null`s nunca são de fato usados. Isso é um comportamento do `@Configuration` do Spring Framework, não do Spring Batch — a versão do Batch é irrelevante aqui. Reclassificar como **melhoria cosmética opcional** (trocar por parâmetros de método deixa o código mais legível, mas não corrige nada quebrado); se decidir não aplicar, manter a nota do `CLAUDE.md` como está.

**`application.properties` (main):** substituir bloco `management.metrics.export.prometheus.*` (RSocket) por:
```
management.otlp.metrics.export.enabled=true
management.otlp.metrics.export.url=http://localhost:4318/v1/metrics
management.otlp.tracing.export.enabled=true
management.otlp.tracing.export.url=http://localhost:4317
```

**`application.properties` (test):**
- Corrigir bloco com typo `spring.jpahibernate.*` → `spring.jpa.hibernate.*` (nunca se aplicou como está)
- Desligar export: `management.otlp.metrics.export.enabled=false`, `management.otlp.tracing.export.enabled=false`

### Fase 3 — Docker / SCDF / OTel

> **Nota de sequenciamento:** esta fase depende de detalhes não confirmados (tags de imagem SCDF/Skipper, URL do descriptor pós-Jakarta) que só se resolvem no deploy. Tratar como um **spike desacoplado** das Fases 1-2: o app (código/testes/Postgres local) pode ser considerado migrado e com critérios de aceite atendidos independentemente do resultado desta fase, para não bloquear o restante da entrega em uma dependência externa não verificável agora.

**`docker/docker-compose.yml`:**
- `dataflow-server`: imagem do SCDF server **2.11.x** (não existe SCDF 3.x; o server é plataforma — não precisa casar versão com o app)
- `app-import`: as URLs `dataflow.spring.io/…-latest` fazem redirect para descriptors pré-Jakarta; trocar por descriptor atual compatível com Boot 4 (confirmar URL nos docs do SCDF no deploy)
- Skipper: **2.11.5-jdk17** (não existe release estável 3.x no Docker Hub — só `3.0.0-SNAPSHOT`; usar a mesma linha 2.11.x do SCDF server, confirmado via API do Docker Hub)

**`docker/docker-compose-prometheus.yml`:**
- Substituir `prometheus-rsocket-proxy` (obsoleto) por **OpenTelemetry Collector** (`otel/opentelemetry-collector-contrib`) recebendo OTLP (4317 gRPC / 4318 HTTP) e exportando para Prometheus
- Manter `prometheus` e `grafana`; ajustar `SPRING_APPLICATION_JSON` do dataflow-server para chaves `management.otlp.*`
- Criar `docker/otel-collector-config.yaml` montado no container

**`docker/Dockerfile`:** base com Java 17+ consistente com Boot 4.0 + SCDF 2.11.x (hoje instala OpenJDK 11)

**README.md:** versões (Java 17, Boot 4.0.8, Batch 6.0.5), env vars do docker-compose, tabela de tools (RSocket Proxy → OTel Collector)

### Fase 4 — Hygiene

- Decidir sobre `application.yml.bkp` (atualizar ou excluir)
- Atualizar `CLAUDE.md` com o novo estado do projeto (versões, arquitetura, comando de teste)

## 6. Verificação

1. **Dry-run antes de qualquer aplicação** (Fase 1.2) — review de diff obrigatório.
2. **Testes (gate principal — H2, sem serviços externos):**
   ```bash
   mvn clean test
   mvn test -Dtest=TransactionRepositoryTest#testSaveTransaction   # iteração
   ```
   Tudo verde no Boot 4.0.8 / Jakarta / Batch 6.0.5.
3. **App local** (Postgres em `localhost:5432/dataflow`, `root`/`rootpw`): `mvn spring-boot:run` — contexto sobe, `souJavaJob` registra, tentativas de export OTLP (connection-refused sem collector = esperado).
4. **Execução do job:** rodar `souJavaJob` (params default ou `baseDir`/`fileName`) e conferir linhas em `transaction` + `reg_type_one/two/three` — valida o fix do wiring `@StepScope`.
5. **Stack docker/SCDF + OTel** (se docker disponível):
   ```bash
   docker-compose -f docker/docker-compose.yml -f docker/docker-compose-postgres.yml -f docker/docker-compose-prometheus.yml up
   ```
   App registra no SCDF, job roda como task, collector recebe OTLP, Prometheus faz scrape. Tags de imagem SCDF/Skipper e URL do descriptor a confirmar na hora.

## 7. Riscos e ressalvas

| Risco | Mitigação |
|---|---|
| Recipies podem deixar estado intermediário inconsistente num `run` só (salto 2.2.6→4.0 é grande) | dry-run + review de diff; fallback para runs sequenciais por versão |
| `MigrateToModularStarters` pode alterar starters no pom além da troca de versão | revisar diff do pom item a item |
| `micrometer-registry-otlp`/tracing em Boot 4.0 podem ter autoconfig diferente da 3.2 | validação na seção 6, item 3 (app local); ajustar chaves de propriedades se necessário |
| URLs descriptores SCDF / tags de imagem não verificadas (docs fora do alcance do proxy) | confirmar contra docs oficiais no deploy |
| `rewrite-spring` tem licença **Moderne Source Available** (não Apache-2.0) | ok para demo interna; anotar se o repo for publicado como template |
| Proxy corporativo bloqueia `repo.spring.io` (403) e Central pode atrasar releases recém-publicadas | versions pinadas só de artifacts confirmados no Central |
| Wrapper Maven corrompido (0 bytes) | reparar na Fase 0 (antes de tudo), não deixar para o final |

## 8. Critérios de aceite

- [x] `mvn clean test` verde com Boot 4.0.8 / Batch 6.0.5 / Jakarta (8/8 testes, `BUILD SUCCESS`)
- [x] App sobe e executa `souJavaJob` persistindo dados no Postgres (validado com H2 nos testes; job completa com status `COMPLETED`, inserts em `transaction`/`reg_type_one/two/three` confirmados via log do Hibernate — execução contra Postgres real não repetida, ambiente local não tinha o serviço rodando)
- [x] Nenhum import `org.springframework.batch.item.*` ou `javax.persistence.*` restante no fonte (só `javax.sql.DataSource`, preservado por design)
- [x] Métricas via OTLP (nenhuma referência RSocket em app/pom/properties; confirmado via `mvn dependency:tree`)
- [x] docker-compose com SCDF 2.11.x + OTel Collector substituindo o RSocket proxy (config validada com `docker compose config`; stack completa não subida ponta a ponta — spike, ver seção 5 Fase 3)
- [x] README e CLAUDE.md atualizados
- [ ] (Opcional, quando sincronizar no Central) plugin OpenRewrite em 6.48.0+ — N/A, plugin removido do pom ao final da migração (já cumpriu seu papel)
