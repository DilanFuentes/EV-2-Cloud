package cl.duoc.pedidos360.notificaciones.listener;

import cl.duoc.pedidos360.messaging.event.PedidoCreadoEvent;
import cl.duoc.pedidos360.messaging.support.ProcesadorMensajes;
import cl.duoc.pedidos360.notificaciones.service.NotificacionService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Dominio notificaciones: escucha PedidoCreado. El nombre de la cola viene de application.yml. */
@Component
public class NotificacionListener {

    private final ProcesadorMensajes procesador;
    private final NotificacionService service;

    public NotificacionListener(ProcesadorMensajes procesador, NotificacionService service) {
        this.procesador = procesador;
        this.service = service;
    }

    @RabbitListener(queues = "${app.rabbitmq.notificaciones.queue}")
    public void onPedidoCreado(Message message, Channel channel) {
        procesador.procesar(message, channel, PedidoCreadoEvent.class, service::enviar);
    }
}
