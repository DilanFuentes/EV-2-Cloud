package cl.duoc.pedidos360.tickets.listener;

import cl.duoc.pedidos360.messaging.event.TicketEvent;
import cl.duoc.pedidos360.messaging.support.ProcesadorMensajes;
import cl.duoc.pedidos360.tickets.service.TicketService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Dominio tickets: escucha las solicitudes de ticket. El nombre de la cola viene de application.yml. */
@Component
public class TicketListener {

    private final ProcesadorMensajes procesador;
    private final TicketService service;

    public TicketListener(ProcesadorMensajes procesador, TicketService service) {
        this.procesador = procesador;
        this.service = service;
    }

    @RabbitListener(queues = "${app.rabbitmq.tickets.queue}")
    public void onSolicitudTicket(Message message, Channel channel) {
        procesador.procesar(message, channel, TicketEvent.class, service::generar);
    }
}
