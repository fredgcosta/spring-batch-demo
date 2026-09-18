# NOTA (fallback Requisito 5.5): a imagem springcloud/spring-cloud-dataflow-server nao publica tag
# com sufixo Java 25 (o maximo disponivel no Docker Hub e -jdk17). Por isso a imagem base
# permanece em jdk17, enquanto o build da aplicacao passa a ter como alvo o Java 25.
# Isso quebra temporariamente a paridade JDK build<->container (Req 5.4) para o stack SCDF.
# Ver a nota "Java 25 e as imagens do Spring Cloud Data Flow" no README.md.
FROM springcloud/spring-cloud-dataflow-server:2.11.5-jdk17

ENTRYPOINT ["java" "-jar" "/maven/spring-cloud-dataflow-server.jar"]