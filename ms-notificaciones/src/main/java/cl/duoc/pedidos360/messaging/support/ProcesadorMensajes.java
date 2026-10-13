package cl.duoc.pedidos360.messaging.support;

import cl.duoc.pedidos360.config.ConsumoProperties;
import cl.duoc.pedidos360.messaging.error.ErrorNoRecuperableException;
import cl.duoc.pedidos360.messaging.error.ErrorRecuperableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Manejo explicito de confirmacion de mensajes (ACK manual).
 *  - Exito                  -> basicAck
 *  - Mensaje ilegible o invalido (no recuperable) -> basicNack(requeue=false) -> DLQ
 *  - Error recuperable      -> reintenta hasta max-intentos; si se agotan -> basicNack -> DLQ
 * Cada decision queda registrada en el log.
 */
@Component
public class ProcesadorMensajes {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajes.class);

    private final ObjectMapper mapper;
    private final ConsumoProperties props;

    public ProcesadorMensajes(ObjectMapper mapper, ConsumoProperties props) {
        this.mapper = mapper;
        this.props = props;
    }

    public <T> void procesar(Message message, Channel channel, Class<T> tipo, Consumer<T> logica) {
        long tag = message.getMessageProperties().getDeliveryTag();
        String cola = message.getMessageProperties().getConsumerQueue();

        T evento;
        try {
            evento = mapper.readValue(message.getBody(), tipo);
        } catch (IOException e) {
            log.error("[{}] Mensaje ilegible, se envia a la DLQ: {}", cola, e.getMessage());
            rechazar(channel, tag, cola);
            return;
        }

        for (int intento = 1; ; intento++) {
            try {
                logica.accept(evento);
                confirmar(channel, tag, cola);
                return;
            } catch (ErrorNoRecuperableException e) {
                log.error("[{}] Error no recuperable, se envia a la DLQ: {}", cola, e.getMessage());
                rechazar(channel, tag, cola);
                return;
            } catch (ErrorRecuperableException e) {
                if (intento >= props.maxIntentos()) {
                    log.error("[{}] Reintentos agotados ({}), se envia a la DLQ: {}",
                            cola, props.maxIntentos(), e.getMessage());
                    rechazar(channel, tag, cola);
                    return;
                }
                log.warn("[{}] Error recuperable (intento {}/{}): {}",
                        cola, intento, props.maxIntentos(), e.getMessage());
                esperar(props.esperaMs());
            } catch (RuntimeException e) {
                log.error("[{}] Error inesperado, se envia a la DLQ", cola, e);
                rechazar(channel, tag, cola);
                return;
            }
        }
    }

    private void confirmar(Channel channel, long tag, String cola) {
        try {
            channel.basicAck(tag, false);
            log.info("[{}] Mensaje procesado y confirmado (ACK)", cola);
        } catch (IOException e) {
            log.error("[{}] No se pudo confirmar (ACK) el mensaje", cola, e);
        }
    }

    private void rechazar(Channel channel, long tag, String cola) {
        try {
            channel.basicNack(tag, false, false); // requeue=false -> dead-letter exchange -> DLQ
        } catch (IOException e) {
            log.error("[{}] No se pudo rechazar (NACK) el mensaje", cola, e);
        }
    }

    private void esperar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
