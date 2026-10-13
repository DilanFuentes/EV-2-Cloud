package cl.duoc.pedidos360.auditoria.listener;

import cl.duoc.pedidos360.auditoria.service.AuditoriaService;
import cl.duoc.pedidos360.messaging.event.PedidoCreadoEvent;
import cl.duoc.pedidos360.messaging.support.ProcesadorMensajes;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Dominio auditoria: escucha PedidoCreado en su propia cola (auditoria.queue). */
@Component
public class AuditoriaListener {

    private final ProcesadorMensajes procesador;
    private final AuditoriaService service;

    public AuditoriaListener(ProcesadorMensajes procesador, AuditoriaService service) {
        this.procesador = procesador;
        this.service = service;
    }

    @RabbitListener(queues = "${app.rabbitmq.auditoria.queue}")
    public void onPedidoCreado(Message message, Channel channel) {
        procesador.procesar(message, channel, PedidoCreadoEvent.class, service::registrar);
    }
}
