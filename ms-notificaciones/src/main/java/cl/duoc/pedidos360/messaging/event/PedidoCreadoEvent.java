package cl.duoc.pedidos360.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoCreadoEvent(
        Long pedidoId,
        Long clienteId,
        BigDecimal total,
        String estado,
        LocalDateTime fechaCreacion) {}
