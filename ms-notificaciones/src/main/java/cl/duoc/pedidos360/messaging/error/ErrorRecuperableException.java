package cl.duoc.pedidos360.messaging.error;

/** Falla transitoria (ej. servicio externo caido): se reintenta y, si se agotan, va a la DLQ. */
public class ErrorRecuperableException extends RuntimeException {
    public ErrorRecuperableException(String mensaje) {
        super(mensaje);
    }
}
