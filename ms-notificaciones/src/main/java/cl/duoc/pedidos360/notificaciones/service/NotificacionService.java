package cl.duoc.pedidos360.notificaciones.service;

import cl.duoc.pedidos360.messaging.error.ErrorNoRecuperableException;
import cl.duoc.pedidos360.messaging.error.ErrorRecuperableException;
import cl.duoc.pedidos360.messaging.event.PedidoCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void enviar(PedidoCreadoEvent evento) {
        if (evento.pedidoId() == null || evento.total() == null || evento.total().signum() <= 0) {
            throw new ErrorNoRecuperableException("Evento de pedido invalido: " + evento);
        }
        // Simulacion de falla transitoria del servicio de correo (cliente 999)
        if (Long.valueOf(999L).equals(evento.clienteId())) {
            throw new ErrorRecuperableException("Servicio de correo no disponible (simulado)");
        }
        log.info("Notificacion enviada: cliente={} pedido={} total={}",
                evento.clienteId(), evento.pedidoId(), evento.total());
    }
}
