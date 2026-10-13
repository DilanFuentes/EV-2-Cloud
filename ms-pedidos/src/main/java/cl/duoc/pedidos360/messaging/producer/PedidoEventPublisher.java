package cl.duoc.pedidos360.messaging.producer;

import cl.duoc.pedidos360.config.MensajeriaProperties;
import cl.duoc.pedidos360.messaging.event.PedidoCreadoEvent;
import cl.duoc.pedidos360.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor del evento PedidoCreado (exchange topic).
 * Los nombres vienen de MensajeriaProperties. Si RabbitMQ falla, se registra el error
 * y el pedido ya guardado no se ve afectado.
 */
@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensajeriaProperties props;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate, MensajeriaProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publicarPedidoCreado(Pedido pedido) {
        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                pedido.getId(), pedido.getClienteId(), pedido.getTotal(),
                pedido.getEstado(), pedido.getFecha());
        try {
            // notificaciones y auditoria comparten la routing key pedido.creado
            rabbitTemplate.convertAndSend(
                    props.exchanges().pedidos(), props.notificaciones().routingKey(), evento);
            log.info("Evento PedidoCreado publicado: pedidoId={}", pedido.getId());
        } catch (AmqpException e) {
            log.error("No se pudo publicar PedidoCreado: pedidoId={}", pedido.getId(), e);
        }
    }
}
