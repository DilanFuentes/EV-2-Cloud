package cl.duoc.pedidos360.messaging.producer;

import cl.duoc.pedidos360.config.MensajeriaProperties;
import cl.duoc.pedidos360.messaging.event.TicketEvent;
import cl.duoc.pedidos360.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Productor de solicitudes de ticket (exchange direct). */
@Component
public class TicketEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TicketEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties props;

    public TicketEventPublisher(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publicarSolicitudTicket(Pedido pedido) {
        TicketEvent evento = new TicketEvent(
                pedido.getId(), pedido.getClienteId(), pedido.getTotal(), LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(
                    props.exchanges().tickets(), props.tickets().routingKey(), evento);
            log.info("Solicitud de ticket publicada: pedidoId={}", pedido.getId());
        } catch (AmqpException e) {
            log.error("No se pudo publicar la solicitud de ticket: pedidoId={}", pedido.getId(), e);
        }
    }
}
