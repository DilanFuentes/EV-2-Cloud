package cl.duoc.pedidos360.service;

import cl.duoc.pedidos360.messaging.producer.PedidoEventPublisher;
import cl.duoc.pedidos360.messaging.producer.TicketEventPublisher;
import cl.duoc.pedidos360.model.DetallePedido;
import cl.duoc.pedidos360.model.Pedido;
import cl.duoc.pedidos360.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher pedidoPublisher;
    private final TicketEventPublisher ticketPublisher;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoEventPublisher pedidoPublisher,
                         TicketEventPublisher ticketPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoPublisher = pedidoPublisher;
        this.ticketPublisher = ticketPublisher;
    }

    public Pedido crear(Pedido pedido) {
        // Logica original: enlazar detalles con su pedido y calcular el total
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido item : pedido.getItems()) {
            item.setPedido(pedido);
            total = total.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
        }
        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);

        // Mensajeria asincrona: solo despues de guardar
        pedidoPublisher.publicarPedidoCreado(guardado);
        ticketPublisher.publicarSolicitudTicket(guardado);
        return guardado;
    }
}
