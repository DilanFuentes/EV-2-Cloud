package cl.duoc.pedidos360.admin.exception;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> detalles = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> detalles.put(e.getField(), e.getDefaultMessage()));
        return respuesta(HttpStatus.BAD_REQUEST, "Parametros invalidos", detalles);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> estado(ResponseStatusException ex) {
        return respuesta(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    @ExceptionHandler(AmqpConnectException.class)
    public ResponseEntity<Map<String, Object>> sinConexion(AmqpConnectException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "No hay conexion con RabbitMQ", null);
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> rabbit(AmqpException ex) {
        return respuesta(HttpStatus.CONFLICT,
                "RabbitMQ rechazo la operacion (puede que el recurso exista con otra configuracion)", null);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje, Object detalles) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("status", status.value());
        cuerpo.put("error", mensaje);
        if (detalles != null) {
            cuerpo.put("detalles", detalles);
        }
        return ResponseEntity.status(status).body(cuerpo);
    }
}
