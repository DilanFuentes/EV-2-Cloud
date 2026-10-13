package cl.duoc.pedidos360.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ de ms-pedidos. Solo declara beans; los nombres vienen de application.yml.
 * Topic:  pedidos.exchange -> notificaciones.queue y auditoria.queue (routing key pedido.creado)
 * Direct: tickets.exchange -> tickets.queue (routing key ticket.generar)
 * Cada cola tiene una DLQ asociada mediante un DLX.
 */
@Configuration
@EnableConfigurationProperties(MensajeriaProperties.class)
public class RabbitMQConfig {

    private final MensajeriaProperties props;

    public RabbitMQConfig(MensajeriaProperties props) {
        this.props = props;
    }

    // ---- 1. EXCHANGES ----
    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(props.exchanges().pedidos(), true, false);
    }

    @Bean
    public DirectExchange ticketsExchange() {
        return new DirectExchange(props.exchanges().tickets(), true, false);
    }

    @Bean
    public DirectExchange pedidosDlx() {
        return new DirectExchange(props.exchanges().pedidosDlx(), true, false);
    }

    @Bean
    public DirectExchange ticketsDlx() {
        return new DirectExchange(props.exchanges().ticketsDlx(), true, false);
    }

    // ---- 2. COLAS Y DLQ ----
    @Bean
    public Queue notificacionesQueue() {
        return colaConDlq(props.notificaciones(), props.exchanges().pedidosDlx());
    }

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(props.notificaciones().dlq()).build();
    }

    @Bean
    public Queue auditoriaQueue() {
        return colaConDlq(props.auditoria(), props.exchanges().pedidosDlx());
    }

    @Bean
    public Queue auditoriaDlq() {
        return QueueBuilder.durable(props.auditoria().dlq()).build();
    }

    @Bean
    public Queue ticketsQueue() {
        return colaConDlq(props.tickets(), props.exchanges().ticketsDlx());
    }

    @Bean
    public Queue ticketsDlq() {
        return QueueBuilder.durable(props.tickets().dlq()).build();
    }

    private Queue colaConDlq(MensajeriaProperties.Canal canal, String dlx) {
        return QueueBuilder.durable(canal.queue())
                .deadLetterExchange(dlx)
                .deadLetterRoutingKey(canal.dlqRoutingKey())
                .build();
    }

    // ---- 3. BINDINGS ----
    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange())
                .with(props.notificaciones().routingKey());
    }

    @Bean
    public Binding auditoriaBinding() {
        return BindingBuilder.bind(auditoriaQueue()).to(pedidosExchange())
                .with(props.auditoria().routingKey());
    }

    @Bean
    public Binding ticketsBinding() {
        return BindingBuilder.bind(ticketsQueue()).to(ticketsExchange())
                .with(props.tickets().routingKey());
    }

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(pedidosDlx())
                .with(props.notificaciones().dlqRoutingKey());
    }

    @Bean
    public Binding auditoriaDlqBinding() {
        return BindingBuilder.bind(auditoriaDlq()).to(pedidosDlx())
                .with(props.auditoria().dlqRoutingKey());
    }

    @Bean
    public Binding ticketsDlqBinding() {
        return BindingBuilder.bind(ticketsDlq()).to(ticketsDlx())
                .with(props.tickets().dlqRoutingKey());
    }

    // ---- 4. CONVERTER JSON ----
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // ---- 5. DECLARACION AL ARRANCAR ----
    @Bean
    public ApplicationRunner declararTopologia(AmqpAdmin amqpAdmin) {
        return args -> amqpAdmin.initialize();
    }
}
