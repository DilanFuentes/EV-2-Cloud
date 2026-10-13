package cl.duoc.pedidos360.messaging.error;

/** Mensaje invalido: reintentar no sirve, va directo a la DLQ. */
public class ErrorNoRecuperableException extends RuntimeException {
    public ErrorNoRecuperableException(String mensaje) {
        super(mensaje);
    }
}
