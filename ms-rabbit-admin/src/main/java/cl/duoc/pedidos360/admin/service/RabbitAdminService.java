package cl.duoc.pedidos360.admin.service;

import cl.duoc.pedidos360.admin.dto.BindingRequest;
import cl.duoc.pedidos360.admin.dto.ExchangeRequest;
import cl.duoc.pedidos360.admin.dto.QueueRequest;
import cl.duoc.pedidos360.admin.dto.QueueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Toda la logica de administracion de RabbitMQ vive aqui.
 * Los controladores solo llaman a estos metodos y no conocen la libreria de RabbitMQ.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    // ---------- Colas ----------
    public QueueResponse crearCola(QueueRequest req) {
        validarNombreNoReservado(req.name());
        if (req.deadLetterRoutingKey() != null && req.deadLetterExchange() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "deadLetterRoutingKey requiere deadLetterExchange");
        }
        boolean durable = req.durable() == null || req.durable();
        QueueBuilder builder = durable ? QueueBuilder.durable(req.name()) : QueueBuilder.nonDurable(req.name());
        if (req.deadLetterExchange() != null) {
            builder.deadLetterExchange(req.deadLetterExchange());
        }
        if (req.deadLetterRoutingKey() != null) {
            builder.deadLetterRoutingKey(req.deadLetterRoutingKey());
        }
        if (req.ttlMs() != null) {
            builder.ttl(req.ttlMs());
        }
        Queue cola = builder.build();
        amqpAdmin.declareQueue(cola);
        log.info("Cola creada: {}", req.name());
        return obtenerCola(req.name());
    }

    public QueueResponse obtenerCola(String nombre) {
        QueueInformation info = amqpAdmin.getQueueInfo(nombre);
        if (info == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cola '" + nombre + "' no existe");
        }
        return new QueueResponse(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    public void eliminarCola(String nombre) {
        obtenerCola(nombre); // lanza 404 si no existe
        amqpAdmin.deleteQueue(nombre);
        log.info("Cola eliminada: {}", nombre);
    }

    // ---------- Exchanges ----------
    public void crearExchange(ExchangeRequest req) {
        validarNombreNoReservado(req.name());
        boolean durable = req.durable() == null || req.durable();
        Exchange exchange = switch (req.type()) {
            case "direct" -> new DirectExchange(req.name(), durable, false);
            case "topic" -> new TopicExchange(req.name(), durable, false);
            case "fanout" -> new FanoutExchange(req.name(), durable, false);
            default -> new HeadersExchange(req.name(), durable, false);
        };
        amqpAdmin.declareExchange(exchange);
        log.info("Exchange creado: {} ({})", req.name(), req.type());
    }

    public void eliminarExchange(String nombre) {
        validarNombreNoReservado(nombre);
        amqpAdmin.deleteExchange(nombre);
        log.info("Exchange eliminado: {}", nombre);
    }

    // ---------- Bindings ----------
    public void crearBinding(BindingRequest req) {
        obtenerCola(req.queue()); // 404 si la cola no existe
        amqpAdmin.declareBinding(new Binding(
                req.queue(), Binding.DestinationType.QUEUE, req.exchange(), req.routingKey(), null));
        log.info("Binding creado: {} -> {} [{}]", req.exchange(), req.queue(), req.routingKey());
    }

    public void eliminarBinding(BindingRequest req) {
        amqpAdmin.removeBinding(new Binding(
                req.queue(), Binding.DestinationType.QUEUE, req.exchange(), req.routingKey(), null));
        log.info("Binding eliminado: {} -> {} [{}]", req.exchange(), req.queue(), req.routingKey());
    }

    private void validarNombreNoReservado(String nombre) {
        if (nombre.startsWith("amq.")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los nombres que empiezan con 'amq.' estan reservados por RabbitMQ");
        }
    }
}
