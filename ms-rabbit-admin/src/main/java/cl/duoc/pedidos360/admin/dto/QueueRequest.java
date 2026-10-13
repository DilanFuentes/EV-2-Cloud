package cl.duoc.pedidos360.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Solicitud para crear una cola. Solo el nombre es obligatorio. */
public record QueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9._-]*$",
                 message = "El nombre solo admite letras, numeros, punto, guion y guion bajo")
        String name,

        Boolean durable,

        String deadLetterExchange,

        String deadLetterRoutingKey,

        @Min(value = 1, message = "El TTL debe ser mayor a 0 ms")
        Integer ttlMs) {}
