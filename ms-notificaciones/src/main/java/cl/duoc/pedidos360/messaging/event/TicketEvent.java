package cl.duoc.pedidos360.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketEvent(
        Long pedidoId,
        Long clienteId,
        BigDecimal total,
        LocalDateTime fechaSolicitud) {}
