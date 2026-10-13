package cl.duoc.pedidos360.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parametros de reintento de los consumidores (bloque app.consumo de application.yml). */
@ConfigurationProperties(prefix = "app.consumo")
public record ConsumoProperties(int maxIntentos, long esperaMs) {}
