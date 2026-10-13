package cl.duoc.pedidos360.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal total;
    private List<DetallePedidoDTO> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<DetallePedidoDTO> getItems() { return items; }
    public void setItems(List<DetallePedidoDTO> items) { this.items = items; }
}
