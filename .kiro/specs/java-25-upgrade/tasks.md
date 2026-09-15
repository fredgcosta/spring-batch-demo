# Implementation Plan: Atualização para Java 25

## Overview

Este plano converte o design da migração de **Java 17 → Java 25** em uma sequência de tarefas de código/configuração incrementais, seguindo o fluxo controlado do design: configurar o `rewrite-maven-plugin` → `dryRun` → revisar → `run` → ajustes manuais → validar (build/testes/job) → revisar diff → commit. A árvore de trabalho do Git é a rede de segurança para rollback.

As tarefas respeitam a ordenação dos três caminhos de aquisição do artefato da recipe (A: repositório autenticado; B: build do fonte; C: migração manual) e cobrem os ajustes que o OpenRewrite não faz (propriedade `java.version`, alinhamento do Lombok, imagens Docker/compose, `README.md`, inventário de flags de JVM). Os testes baseados em propriedades (jqwik + `spring-batch-test` sobre H2) validam a preservação de comportamento do `souJavaJob` (Property 1 e Property 2).

## Tasks

- [x] 1. Configurar o Rewrite_Maven_Plugin e o repositório de artefatos no `pom.xml`
  - [x] 1.1 Declarar o `rewrite-maven-plugin` com a Recipe_UpgradeToJava25 ativa
    - Adicionar o plugin `org.openrewrite.maven:rewrite-maven-plugin` no bloco `<build><plugins>` do `pom.xml`, com `<activeRecipes>` contendo `org.openrewrite.java.migrate.UpgradeToJava25`
    - Adicionar a dependência `org.openrewrite.recipe:rewrite-migrate-java` no bloco `<dependencies>` do plugin
    - Fixar as versões de `rewrite-maven-plugin` e `rewrite-migrate-java` em propriedades (`<rewrite-maven-plugin.version>` / `<rewrite-migrate-java.version>`), escolhendo o par compatível que publica `UpgradeToJava25`
    - _Requirements: 1.1_

  - [x] 1.2 Declarar o Code_Genome_Repository e a configuração de credenciais (Caminho A)
    - Adicionar o repositório `https://artifacts.codegenomeproject.org/maven` (com um `id` como `code-genome`) em `<repositories>` e `<pluginRepositories>` do `pom.xml`
    - Documentar em `.m2/settings.xml` de exemplo (não comitado no repo com valores reais) a entrada `<server>` com `id` casando o do repositório e `username`/`password` referenciando `${env.CODEGENOME_USER}` / `${env.CODEGENOME_TOKEN}`
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ]* 1.3 Documentar a restrição de licença/autenticação e impacto offline/CI
    - Registrar no `README.md` que a Recipe_UpgradeToJava25 está sob a Moderne Source Available License, distribuída pelo Code_Genome_Repository que exige autenticação
    - Descrever o impacto da autenticação na execução offline e em CI e qual caminho alternativo aplicar em cada situação (A → B → C)
    - _Requirements: 2.1, 2.6_

- [x] 2. Executar o Dry_Run e decidir o caminho de aquisição
  - [x] 2.1 Garantir árvore de trabalho limpa e executar o Dry_Run
    - Confirmar que não há alterações não commitadas (rede de segurança de rollback)
    - Executar `./mvnw rewrite:dryRun` e capturar o `Diff_Migracao` (patch + saída de console) sem modificar arquivos-fonte
    - _Requirements: 1.2_

  - [x]* 2.2 Aplicar o fallback de aquisição do artefato se o Caminho A falhar
    - Se a resolução via Code_Genome_Repository falhar por autenticação/artefato não encontrado, construir a recipe a partir do código-fonte e instalar em `~/.m2` (Caminho B)
    - Se nem A nem B forem viáveis, seguir a migração pelo caminho manual (Caminho C) coberto pelas tarefas 4, 5, 6 e 8
    - _Requirements: 2.4, 2.5_

- [x] 3. Checkpoint — Avaliar o Dry_Run antes de aplicar
  - Verificar que o Dry_Run terminou com `BUILD SUCCESS` e sem erros de recipe; se houve `BUILD FAILURE` ou erro de recipe, interromper sem executar `rewrite:run` e preservar os fontes. Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 1.4_

- [x] 4. Aplicar a recipe e realizar os ajustes manuais de build
  - [x] 4.1 Aplicar a recipe com `rewrite:run`
    - Após o gate do Dry_Run, executar `./mvnw rewrite:run` para aplicar o `Diff_Migracao` aos fontes e ao `pom.xml`
    - Se `rewrite:run` terminar com `BUILD FAILURE`, restaurar `pom.xml` e fontes ao estado anterior via `git restore`/`git checkout -- .` e indicar a falha
    - _Requirements: 1.3, 1.5, 1.6, 1.7_

  - [x] 4.2 Definir explicitamente `java.version=25` no `pom.xml`
    - Alterar a propriedade `<java.version>` de `17` para `25`
    - Corrigir manualmente qualquer configuração de build que ainda referencie versão de Java anterior a `25`
    - _Requirements: 3.1, 3.2_

  - [x] 4.3 Alinhar a versão do Lombok entre dependência e annotation processor
    - Introduzir a propriedade `<lombok.version>` com uma versão de Lombok com suporte a JDK 25
    - Referenciar `${lombok.version}` no `<version>` do `annotationProcessorPaths` do `maven-compiler-plugin` (hoje fixo em `1.18.44`) e, se necessário, na dependência `org.projectlombok:lombok`, eliminando divergência
    - _Requirements: 3.3, 4.2_

- [x] 5. Verificar compatibilidade de dependências e plugins com Java 25
  - [x] 5.1 Verificar e ajustar dependências/plugins incompatíveis
    - Compilar e inicializar Spring Boot 4.0.8 / Batch 6.0.5 / Cloud 2025.1.3 / Cloud Task 2.5.0, OTel/Micrometer (`micrometer-registry-otlp`, `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`), `logstash-logback-encoder` 8.1, `maven-compiler-plugin` e `spring-boot-maven-plugin` sob JDK 25
    - Onde surgir erro atribuível à incompatibilidade com Java 25, elevar a versão do artefato até eliminá-lo (bump apenas quando necessário)
    - _Requirements: 4.1, 4.4, 4.5, 4.6_

  - [x] 5.2 Tratar APIs depreciadas/removidas remanescentes
    - Aplicar manualmente as substituições que a recipe registrou sem migração automática (classe/método + arquivo/linha), preservando as substituições já feitas pela recipe
    - Garantir que o build no Java 25 compile sem avisos de API depreciada/removida
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ]* 5.3 Inventariar e ajustar flags de JVM afetadas
    - Inventariar argumentos de JVM em `application.properties`, `docker/Dockerfile`, `docker-compose*.yml` e `README.md`
    - Substituir flags com equivalente suportado (registrando original e substituta) e remover flags sem equivalente (registrando o arquivo afetado); focar nas flags controladas pelo Projeto
    - _Requirements: 7.4, 7.5_

- [x] 6. Checkpoint — Build compila no Java 25
  - Executar `./mvnw clean package` e confirmar `BUILD SUCCESS`, bytecode alvo 25 e ausência de erros de processamento de anotações do Lombok. Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 3.4, 3.5, 6.1, 6.2_

- [x] 7. Atualizar imagens Docker e referências de JDK
  - [x] 7.1 Atualizar a imagem base no `docker/Dockerfile`
    - Trocar `FROM springcloud/spring-cloud-dataflow-server:2.11.5-jdk17` por uma tag cujo sufixo de JDK indique Java 25, sem deixar referência a `jdk17`
    - Se não existir tag `jdk25` publicada para a imagem, manter a referência atual e registrar nota no `README.md` sobre a indisponibilidade (fallback)
    - _Requirements: 5.1, 5.4, 5.5_

  - [x] 7.2 Atualizar tags e defaults de variáveis nos arquivos docker-compose
    - Em `docker/docker-compose.yml`, `docker/docker-compose-postgres.yml` e `docker/docker-compose-prometheus.yml`, substituir sufixos de JDK 17 por Java 25 em tags de imagem e defaults de `SKIPPER_VERSION` / `DATAFLOW_VERSION` (ex.: `${SKIPPER_VERSION:-2.11.5-jdk17}`) e comentários
    - Aplicar o mesmo fallback do 7.1 quando a tag `jdk25` não existir
    - _Requirements: 5.2, 5.4, 5.5_

  - [x] 7.3 Atualizar menções de JDK no `README.md`
    - Substituir "OpenJDK 17" e comandos que fixam `SKIPPER_VERSION=2.11.5-jdk17` pelas referências de Java 25, sem deixar menção à versão anterior de JDK
    - _Requirements: 5.3, 5.4_

- [x] 8. Adicionar testes baseados em propriedades para o Job_Batch
  - [x] 8.1 Adicionar a dependência jqwik e a infraestrutura de teste do job
    - Adicionar `net.jqwik:jqwik` como dependência de teste no `pom.xml`
    - Criar a base de teste do `souJavaJob` usando `spring-batch-test` sobre H2 (OTLP desabilitado via `src/test/resources/application.properties`), com um gerador de arquivos de entrada de largura fixa válidos (cabeçalho `0000`, N blocos `0100`+`0101`+`0102`+`0103`, rodapé `9999`)
    - _Requirements: 6.3_

  - [x]* 8.2 Escrever o teste de propriedade para o grafo de entidades
    - **Property 1: Processamento do job produz o grafo de entidades esperado**
    - **Validates: Requirements 6.5**
    - Um único teste jqwik (mín. 100 iterações) que gera a entrada, executa o job e verifica `count(Transaction) == count(linhas 0100)` e, para cada `Transaction`, a existência de `RegTypeOne/Two/Three` com o mesmo id
    - Incluir o comentário de tag: `// Feature: java-25-upgrade, Property 1: Processamento do job produz o grafo de entidades esperado`

  - [x]* 8.3 Escrever o teste de propriedade de preservação de comportamento
    - **Property 2: Preservação de comportamento entre JDK 17 e JDK 25**
    - **Validates: Requirements 6.5**
    - Um único teste jqwik (mín. 100 iterações) que verifica que o grafo persistido depende apenas da entrada, ancorado por um baseline determinístico capturado no Java 17 (ou execução da suíte em ambos os JDKs)
    - Incluir o comentário de tag: `// Feature: java-25-upgrade, Property 2: Preservação de comportamento entre JDK 17 e JDK 25`

  - [x]* 8.4 Escrever teste de exemplo/edge case para falha do job
    - Fornecer entrada malformada (bloco incompleto, prefixo inválido) e verificar que o job encerra com status de falha indicando a etapa responsável
    - _Requirements: 6.6_

- [x] 9. Checkpoint — Suíte de testes verde no Java 25
  - Executar `./mvnw test` e confirmar que todos os testes (incluindo os de propriedade e o edge case) concluem sem falhas nem erros. Ensure all tests pass, ask the user if questions arise.
  - _Requirements: 6.3, 6.4_

- [x] 10. Validar a execução do Job_Batch no Java 25
  - [x]* 10.1 Executar o `souJavaJob` sobre o arquivo de entrada padrão
    - Executar o job sobre `exemplo-sou-java-10.txt` (via teste de integração automatizado com `spring-batch-test`/H2) e confirmar a persistência de todas as `Transaction` e das `RegType*` correspondentes com id compartilhado
    - _Requirements: 6.5, 6.6_

- [ ] 11. Revisar o Diff_Migracao e registrar o commit
  - [ ]* 11.1 Revisar o diff e ajustar alterações indesejadas
    - Apresentar todas as alterações pendentes (`git status` + `git diff` / patch do rewrite) para revisão, impedindo commit automático
    - Reverter/ajustar seletivamente alterações indesejadas (`git restore -p` ou edição pontual) preservando as demais
    - _Requirements: 8.1, 8.2, 8.5_

  - [ ] 11.2 Registrar um único commit convencional com o gate de build+testes
    - Confirmar build (`./mvnw clean package`) e testes (`./mvnw test`) verdes sobre o estado do diff antes de commitar; se falharem, bloquear o commit e indicar a falha mantendo as alterações não commitadas
    - Registrar exclusivamente as alterações aprovadas em um único commit com mensagem iniciando por prefixo de conventional-commit (`chore:`)
    - _Requirements: 8.3, 8.4_

## Notes

- Tarefas marcadas com `*` são opcionais/manuais e podem ser puladas para um MVP mais rápido; incluem escrita de testes, verificação manual, documentação e revisão do diff.
- Cada tarefa referencia requisitos específicos para rastreabilidade; as tarefas 8.2 e 8.3 referenciam explicitamente as Correctness Properties do design (Property 1 e Property 2) e usam jqwik com os comentários de tag exigidos.
- Os checkpoints (3, 6, 9) garantem validação incremental nos pontos de gate do fluxo de migração.
- O fluxo respeita a ordenação dos caminhos de aquisição da recipe (A → B → C) e usa o Git como rede de segurança de rollback.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["2.2"] },
    { "id": 3, "tasks": ["4.1"] },
    { "id": 4, "tasks": ["4.2", "4.3"] },
    { "id": 5, "tasks": ["5.1", "5.2", "5.3"] },
    { "id": 6, "tasks": ["7.1", "7.2", "7.3", "8.1"] },
    { "id": 7, "tasks": ["8.2", "8.3", "8.4"] },
    { "id": 8, "tasks": ["10.1"] },
    { "id": 9, "tasks": ["11.1"] },
    { "id": 10, "tasks": ["11.2"] }
  ]
}
```
