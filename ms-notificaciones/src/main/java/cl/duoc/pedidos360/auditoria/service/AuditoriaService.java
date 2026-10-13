package cl.duoc.pedidos360.auditoria.service;

import cl.duoc.pedidos360.messaging.error.ErrorNoRecuperableException;
import cl.duoc.pedidos360.messaging.event.PedidoCreadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    public void registrar(PedidoCreadoEvent evento) {
        if (evento.pedidoId() == null) {
            throw new ErrorNoRecuperableException("Evento sin pedidoId: " + evento);
        }
        log.info("AUDITORIA pedido={} cliente={} total={} estado={} fecha={}",
                evento.pedidoId(), evento.clienteId(), evento.total(),
                evento.estado(), evento.fechaCreacion());
    }
}
