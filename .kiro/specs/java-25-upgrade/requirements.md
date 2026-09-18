# Requirements Document

## Introduction

Esta funcionalidade descreve a atualização da versão do Java do projeto de **Java 17** para **Java 25**, utilizando o **OpenRewrite** como mecanismo principal de migração automatizada sempre que possível. O projeto é uma aplicação Spring Boot 4.0.8 / Spring Batch 6.0.5 / Spring Cloud 2025.1.3 / Spring Cloud Task 2.5.0.RELEASE, empacotada como Spring Cloud Task (`@EnableTask`) e construída com Maven (wrapper `./mvnw`).

A migração abrange: a execução da recipe `org.openrewrite.java.migrate.UpgradeToJava25` do OpenRewrite (com etapa de pré-visualização antes de aplicar), o tratamento da restrição de licenciamento/autenticação do repositório Code Genome Project que hospeda os artefatos do OpenRewrite, a atualização do build Maven (propriedade `java.version`), a verificação de compatibilidade de todas as dependências e plugins com o Java 25, a atualização das imagens Docker e referências de JDK, e a validação de que o build e a suíte de testes completa passam no Java 25 com o job do Spring Batch funcionando.

O objetivo é concluir a atualização preservando o comportamento funcional atual da aplicação (fluxo de leitura do arquivo de largura fixa, mapeamento dos registros multi-linha e persistência das entidades JPA).

## Glossary

- **Projeto**: A aplicação Spring Batch/Spring Cloud Task contida neste repositório (`spring-batch-demo`).
- **OpenRewrite**: Ferramenta de refatoração automatizada de código-fonte executada via `rewrite-maven-plugin`.
- **Recipe_UpgradeToJava25**: A recipe composta `org.openrewrite.java.migrate.UpgradeToJava25` do módulo `org.openrewrite.recipe:rewrite-migrate-java`, que atualiza o build para Java 25, atualiza plugins Maven compatíveis e substitui APIs depreciadas/removidas com migração clara. A cadeia é cumulativa (inclui `UpgradeToJava21` e `UpgradeToJava17`), cobrindo a migração de Java 17 → 25.
- **Rewrite_Maven_Plugin**: O plugin Maven `org.openrewrite.maven:rewrite-maven-plugin` usado para executar recipes do OpenRewrite.
- **Dry_Run**: Execução de pré-visualização do OpenRewrite (`./mvnw rewrite:dryRun`) que gera um diff das mudanças propostas sem aplicá-las ao código-fonte.
- **Code_Genome_Repository**: O repositório Maven `https://artifacts.codegenomeproject.org/maven` que distribui os artefatos do OpenRewrite e da Recipe_UpgradeToJava25, exigindo autenticação (usuário/e-mail + token de download).
- **Build_Maven**: O processo de compilação e empacotamento executado por `./mvnw clean package`.
- **Suite_De_Testes**: O conjunto de testes JUnit 5 executado por `./mvnw test`, baseado em H2 em memória, com exportação OTLP desabilitada.
- **Job_Batch**: O job Spring Batch `souJavaJob` definido em `config/BatchConfiguration.java`.
- **Lombok**: A biblioteca Lombok, cujo processador de anotações está fixado na versão `1.18.44` no `maven-compiler-plugin` do `pom.xml`.
- **Imagens_Docker**: As imagens base de JDK e SCDF referenciadas em `docker/Dockerfile` e nos arquivos `docker-compose*.yml`.
- **Diff_Migracao**: O conjunto de alterações de código e configuração produzido pela execução do OpenRewrite.

## Requirements

### Requisito 1: Migração automatizada via OpenRewrite

**História do Usuário:** Como desenvolvedor, quero usar o OpenRewrite para automatizar a migração de Java 17 para Java 25, para reduzir o esforço manual e o risco de erros na atualização.

#### Critérios de Aceitação

1. THE Projeto SHALL configurar o Rewrite_Maven_Plugin no `pom.xml` com a Recipe_UpgradeToJava25 ativa e a dependência `org.openrewrite.recipe:rewrite-migrate-java`.
2. WHEN a migração é iniciada, THE Projeto SHALL executar um Dry_Run (`./mvnw rewrite:dryRun`) que gera o Diff_Migracao proposto sem modificar nenhum arquivo-fonte.
3. WHEN o Dry_Run termina com o build Maven reportando `BUILD SUCCESS` e sem erros de recipe, THE Projeto SHALL aplicar as alterações via `./mvnw rewrite:run`.
4. IF o Dry_Run termina com o build Maven reportando falha (`BUILD FAILURE`) ou com erros de execução de recipe, THEN THE Projeto SHALL interromper a migração sem executar `./mvnw rewrite:run` e SHALL preservar os arquivos-fonte inalterados, indicando a falha ocorrida.
5. THE Recipe_UpgradeToJava25 SHALL atualizar a configuração de build do Projeto para Java 25, definindo a propriedade `java.version` como `25` e o release do compilador como `25`.
6. THE Recipe_UpgradeToJava25 SHALL atualizar os plugins Maven de forma que, após a aplicação via `./mvnw rewrite:run`, o comando `./mvnw clean package` termine com `BUILD SUCCESS` e sem erros de compilação.
7. IF `./mvnw rewrite:run` termina com o build Maven reportando falha (`BUILD FAILURE`), THEN THE Projeto SHALL indicar a falha ocorrida e SHALL manter o `pom.xml` e os arquivos-fonte no estado anterior à aplicação.

### Requisito 2: Restrição de licenciamento e autenticação do repositório de artefatos

**História do Usuário:** Como desenvolvedor, quero tratar a restrição de autenticação e licenciamento dos artefatos do OpenRewrite, para que a migração possa ser executada localmente e em CI sem bloqueios.

#### Critérios de Aceitação

1. THE Projeto SHALL manter documentação que declare explicitamente que a Recipe_UpgradeToJava25 está sob a Moderne Source Available License, que é distribuída pelo Code_Genome_Repository e que o Code_Genome_Repository exige autenticação para download do artefato.
2. THE Projeto SHALL prover configuração de credenciais de acesso (identificação de usuário/e-mail e token de download) para o Code_Genome_Repository nos ambientes de build local e de CI, de modo que o build resolva o artefato da Recipe_UpgradeToJava25 sem erro de autenticação ou de artefato não encontrado.
3. WHEN o build é executado com credenciais válidas do Code_Genome_Repository disponíveis, THE Projeto SHALL obter a Recipe_UpgradeToJava25 a partir do Code_Genome_Repository.
4. IF as credenciais do Code_Genome_Repository não estiverem disponíveis ou a autenticação falhar, THEN THE Projeto SHALL construir a Recipe_UpgradeToJava25 a partir do código-fonte localmente como caminho alternativo, produzindo um artefato utilizável pelo build.
5. IF nem as credenciais válidas nem um build local bem-sucedido da Recipe_UpgradeToJava25 estiverem disponíveis, THEN THE Projeto SHALL executar a migração pelo caminho manual descrito nos requisitos de atualização de build e dependências.
6. THE Projeto SHALL manter documentação que descreva o impacto da exigência de autenticação do Code_Genome_Repository sobre a execução offline e sobre a execução em pipelines de CI, indicando qual caminho alternativo aplicar em cada situação.

### Requisito 3: Atualização manual da configuração de build

**História do Usuário:** Como desenvolvedor, quero atualizar manualmente as configurações de build que o OpenRewrite não cobrir, para garantir que o build tenha como alvo o Java 25 de forma completa.

#### Critérios de Aceitação

1. THE Projeto SHALL definir a propriedade `java.version` como `25` no `pom.xml`.
2. WHEN a Recipe_UpgradeToJava25 concluir e uma configuração de build ainda referenciar uma versão de Java anterior a `25`, THE Projeto SHALL aplicar manualmente no `pom.xml` a alteração que faça essa configuração ter como alvo o Java `25`.
3. THE Projeto SHALL declarar no `annotationProcessorPaths` do `maven-compiler-plugin` a mesma versão de Lombok que a declarada na dependência `org.projectlombok:lombok` do `pom.xml`.
4. WHEN o build for executado com `./mvnw clean package`, THE Projeto SHALL compilar todo o código-fonte tendo como alvo o bytecode Java `25` e concluir sem erros de compilação nem de processamento de anotações do Lombok.
5. IF o build for executado e o processamento de anotações do Lombok falhar ou a compilação não tiver como alvo o Java `25`, THEN THE Projeto SHALL encerrar o build com falha, indicando a configuração de build responsável, sem gerar o artefato empacotado.

### Requisito 4: Compatibilidade de dependências e plugins com Java 25

**História do Usuário:** Como desenvolvedor, quero verificar que todas as dependências e plugins são compatíveis com o Java 25, para evitar falhas de compilação ou de execução após a migração.

#### Critérios de Aceitação

1. WHEN o Build_Maven é executado no Java 25, THE Projeto SHALL compilar e carregar as dependências Spring Boot, Spring Batch e Spring Cloud declaradas no `pom.xml` sem erros de compatibilidade de bytecode nem de inicialização.
2. THE Projeto SHALL declarar a versão do processador de anotações do Lombok no `maven-compiler-plugin` como uma versão que conclua o processamento de anotações sob Java 25 sem erros.
3. WHEN a Suite_De_Testes é executada no Java 25, THE Projeto SHALL inicializar as dependências de observabilidade OpenTelemetry e Micrometer (`micrometer-registry-otlp`, `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`) sem erros de compatibilidade.
4. WHEN o Build_Maven é executado no Java 25, THE Projeto SHALL utilizar a dependência `logstash-logback-encoder` sem erros de compatibilidade de bytecode.
5. WHEN o Build_Maven é executado no Java 25, THE Projeto SHALL executar o `spring-boot-maven-plugin` e o `maven-compiler-plugin` sem erros de compatibilidade com o Java 25.
6. IF uma dependência ou plugin causar erro de compilação, de inicialização ou de execução atribuível à incompatibilidade com Java 25, THEN THE Projeto SHALL atualizar a versão do artefato para uma versão que elimine o erro.

### Requisito 5: Atualização das imagens Docker e referências de JDK

**História do Usuário:** Como desenvolvedor, quero atualizar as imagens Docker e referências de JDK para o Java 25, para que o ambiente de execução conteinerizado seja consistente com o build.

#### Critérios de Aceitação

1. THE Projeto SHALL definir a imagem base no `docker/Dockerfile` como uma tag da imagem `spring-cloud-dataflow-server` cujo sufixo de JDK indique Java 25, de modo que nenhuma referência à tag anterior de JDK 17 permaneça no arquivo.
2. THE Projeto SHALL atualizar, em cada um dos arquivos `docker/docker-compose.yml`, `docker/docker-compose-postgres.yml` e `docker/docker-compose-prometheus.yml`, todas as tags de imagem e valores padrão de variáveis (incluindo `SKIPPER_VERSION` e `DATAFLOW_VERSION`) que referenciem um sufixo de JDK, substituindo o sufixo de Java 17 pelo sufixo correspondente a Java 25.
3. THE Projeto SHALL atualizar no `README.md` todas as menções textuais à versão do JDK usada nas imagens Docker para indicar Java 25, sem deixar nenhuma menção à versão de JDK anterior.
4. THE Projeto SHALL garantir que a versão de JDK referenciada nas imagens Docker seja idêntica à versão de JDK usada no build do projeto (Java 25).
5. IF, para uma imagem referenciada em `docker/Dockerfile` ou nos arquivos `docker-compose*.yml`, não existir tag publicada com sufixo de JDK correspondente a Java 25, THEN THE Projeto SHALL manter a referência de imagem existente inalterada e registrar em `README.md` uma nota indicando a indisponibilidade da tag de Java 25 para aquela imagem.

### Requisito 6: Validação de build, testes e execução do job

**História do Usuário:** Como desenvolvedor, quero garantir que o build, a suíte de testes e o job do Spring Batch funcionam no Java 25, para confirmar que a migração preservou o comportamento da aplicação.

#### Critérios de Aceitação

1. WHEN o Build_Maven (`./mvnw clean package`) é executado no Java 25, THE Projeto SHALL concluir a compilação e o empacotamento reportando `BUILD SUCCESS`.
2. IF o Build_Maven falhar no Java 25, THEN THE Projeto SHALL reportar `BUILD FAILURE` indicando o erro de compilação ou empacotamento, sem gerar o artefato empacotado.
3. WHEN a Suite_De_Testes (`./mvnw test`) é executada no Java 25, THE Projeto SHALL concluir todos os testes sem falhas nem erros.
4. IF algum teste da Suite_De_Testes falhar no Java 25, THEN THE Projeto SHALL reportar a falha indicando o teste responsável.
5. WHEN o Job_Batch é executado no Java 25 sobre o arquivo de entrada padrão de largura fixa, THE Job_Batch SHALL processar todos os registros lógicos de transação e persistir as entidades JPA correspondentes, incluindo as entidades RegType* que compartilham o identificador da transação.
6. IF o Job_Batch falhar durante a leitura, o mapeamento ou a persistência no Java 25, THEN THE Job_Batch SHALL encerrar com status de falha indicando a etapa responsável.

### Requisito 7: Tratamento de APIs depreciadas ou removidas

**História do Usuário:** Como desenvolvedor, quero tratar APIs depreciadas ou removidas e flags de JVM afetadas pela migração, para que o código permaneça funcional e livre de dependências obsoletas no Java 25.

#### Critérios de Aceitação

1. WHEN a Recipe_UpgradeToJava25 identifica uma API depreciada ou removida com substituição equivalente definida, THE Recipe_UpgradeToJava25 SHALL substituir todas as ocorrências pela API equivalente do Java 25 e registrar a API original e a API substituta.
2. IF a Recipe_UpgradeToJava25 identifica uma API depreciada ou removida sem substituição automática definida, THEN THE Recipe_UpgradeToJava25 SHALL registrar a classe/método afetado e a localização (arquivo/linha) sem interromper as demais substituições, e THE Projeto SHALL aplicar a substituição manualmente.
3. WHEN o Build_Maven é executado no Java 25 após o tratamento das APIs, THE Projeto SHALL compilar sem avisos de uso de API depreciada ou removida.
4. IF uma flag de JVM utilizada pela aplicação for alterada ou removida no Java 25 e possuir equivalente suportado, THEN THE Projeto SHALL substituir a flag pelo equivalente suportado e registrar a flag original e a flag substituta.
5. IF uma flag de JVM utilizada pela aplicação for removida no Java 25 sem equivalente suportado, THEN THE Projeto SHALL remover a flag e registrar o arquivo de configuração afetado.

### Requisito 8: Revisão do diff antes do commit

**História do Usuário:** Como desenvolvedor, quero revisar o diff gerado pelo OpenRewrite antes de commitar, para garantir que apenas alterações corretas e pretendidas sejam integradas.

#### Critérios de Aceitação

1. WHEN o Diff_Migracao é gerado, THE Projeto SHALL apresentar todas as alterações pendentes (arquivos adicionados, modificados e removidos) para revisão e SHALL impedir o commit automático até que a revisão seja concluída.
2. WHERE o revisor identificar uma alteração indesejada no Diff_Migracao, THE Projeto SHALL reverter ou ajustar a alteração indicada preservando as demais alterações não indicadas, antes de permitir o commit.
3. WHEN o Diff_Migracao é aprovado pelo revisor, THE Projeto SHALL registrar exclusivamente as alterações aprovadas em um único commit cuja mensagem inicia com um prefixo de conventional-commit (por exemplo, `chore:` ou `fix:`).
4. IF a compilação (`./mvnw clean package`) ou a suíte de testes (`./mvnw test`) falhar sobre o estado do Diff_Migracao, THEN THE Projeto SHALL bloquear o commit e indicar a falha ao revisor, mantendo as alterações não commitadas para ajuste.
5. IF o revisor não aprovar o Diff_Migracao, THEN THE Projeto SHALL manter o estado das alterações sem criar commit e sem descartar as alterações pendentes.
