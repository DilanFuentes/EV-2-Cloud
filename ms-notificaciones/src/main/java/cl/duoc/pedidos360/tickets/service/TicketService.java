package cl.duoc.pedidos360.tickets.service;

import cl.duoc.pedidos360.messaging.error.ErrorNoRecuperableException;
import cl.duoc.pedidos360.messaging.error.ErrorRecuperableException;
import cl.duoc.pedidos360.messaging.event.TicketEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    public void generar(TicketEvent evento) {
        if (evento.pedidoId() == null || evento.total() == null || evento.total().signum() <= 0) {
            throw new ErrorNoRecuperableException("Solicitud de ticket invalida: " + evento);
        }
        // Simulacion de falla transitoria del generador de documentos (cliente 999)
        if (Long.valueOf(999L).equals(evento.clienteId())) {
            throw new ErrorRecuperableException("Generador de documentos no disponible (simulado)");
        }
        log.info("Ticket generado: pedido={} cliente={} total={}",
                evento.pedidoId(), evento.clienteId(), evento.total());
    }
}
