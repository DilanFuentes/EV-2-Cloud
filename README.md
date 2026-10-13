# Pedidos360 - Backend con RabbitMQ (Evaluacion Parcial 3, DSY1107)

Backend de microservicios Spring Boot (Java 17) al que se incorporo mensajeria asincrona con RabbitMQ, sin modificar la logica de negocio existente.

## Arquitectura

- `ms-pedidos` (puerto 8083): productor. Al crear un pedido publica `PedidoCreado` (exchange topic) y una solicitud de ticket (exchange direct).
- `ms-notificaciones` (puerto 8086): consumidores por dominio (`notificaciones`, `tickets`, `auditoria`) con ACK manual, reintentos y DLQ.
- `ms-rabbit-admin` (puerto 8087): API REST para crear y eliminar colas, exchanges y bindings (protegida con JWT).
- `bff`, `ms-clientes`, `ms-productos`: servicios de la evaluacion anterior (JWT de Azure AD).
- `infra/`: `docker-compose.yml` con un cluster RabbitMQ de 2 nodos.

## Topologia RabbitMQ

| Exchange | Tipo | Routing key | Cola | DLQ |
|---|---|---|---|---|
| pedidos.exchange | topic | pedido.creado | notificaciones.queue | notificaciones.dlq |
| pedidos.exchange | topic | pedido.creado | auditoria.queue | auditoria.dlq |
| tickets.exchange | direct | ticket.generar | tickets.queue | tickets.dlq |

Los nombres estan centralizados en `application.yml` y se declaran como beans en `RabbitMQConfig` (ms-pedidos).

## Manejo de errores en consumidores

- Exito: `basicAck`.
- Mensaje invalido (error no recuperable): `basicNack(requeue=false)` y va a la DLQ, con `log.error`.
- Error recuperable: hasta 3 intentos; si se agotan va a la DLQ.

## Como ejecutarlo

1. Cluster RabbitMQ:
```bash
   cd infra
   cp .env.example .env   # editar las credenciales
   docker compose up -d
   docker exec rabbit2 rabbitmqctl stop_app
   docker exec rabbit2 rabbitmqctl reset
   docker exec rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
   docker exec rabbit2 rabbitmqctl start_app
```
   Panel de administracion: http://localhost:15672
2. Cada microservicio (desde su carpeta):
```bash
   RABBITMQ_PASS=<clave> mvn spring-boot:run
```
   `ms-pedidos` ademas necesita PostgreSQL y la variable `DB_PASSWORD`.
   Para pruebas locales sin JWT existe el perfil `local` (`SPRING_PROFILES_ACTIVE=local`), que no debe usarse fuera del equipo del desarrollador.

## Endpoints del administrador

- `POST /queues`, `GET /queues/{name}`, `DELETE /queues/{name}`
- `POST /exchanges`, `DELETE /exchanges/{name}`
- `POST /bindings`, `DELETE /bindings`

## Evidencias

Capturas de funcionamiento en la carpeta `evidencias/`.
