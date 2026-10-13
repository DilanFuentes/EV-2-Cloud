package cl.duoc.pedidos360.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Lee el bloque app.rabbitmq de application.yml.
 * Es la unica fuente de nombres de exchanges, colas y routing keys.
 */
@ConfigurationProperties(prefix = "app.rabbitmq")
public record MensajeriaProperties(
        Exchanges exchanges,
        Canal notificaciones,
        Canal auditoria,
        Canal tickets) {

    public record Exchanges(String pedidos, String pedidosDlx, String tickets, String ticketsDlx) {}

    public record Canal(String queue, String routingKey, String dlq, String dlqRoutingKey) {}
}
