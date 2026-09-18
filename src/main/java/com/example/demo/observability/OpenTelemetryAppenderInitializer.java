package com.example.demo.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Conecta a instância {@link OpenTelemetry} autoconfigurada pelo Spring Boot ao
 * {@link OpenTelemetryAppender} declarado no logback-spring.xml.
 *
 * <p>O Logback é inicializado antes do contexto Spring, então o appender começa sem uma
 * instância OTel e descarta eventos até que {@code install(...)} seja chamado. Este bean
 * roda no startup (via {@link InitializingBean}) e faz essa ligação, passando a exportar
 * os logs pela API OpenTelemetry (e, com endpoint configurado, via OTLP).
 */
@Component
public class OpenTelemetryAppenderInitializer implements InitializingBean {

  private final OpenTelemetry openTelemetry;

  public OpenTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
    this.openTelemetry = openTelemetry;
  }

  @Override
  public void afterPropertiesSet() {
    OpenTelemetryAppender.install(this.openTelemetry);
  }
}
