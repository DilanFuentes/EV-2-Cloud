package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** Une una cola a un exchange mediante una routing key. */
public record BindingRequest(
        @NotBlank(message = "La cola es obligatoria") String queue,
        @NotBlank(message = "El exchange es obligatorio") String exchange,
        @NotBlank(message = "La routing key es obligatoria") String routingKey) {}
