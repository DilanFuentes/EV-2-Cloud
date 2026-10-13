package cl.duoc.pedidos360.admin.controller;

import cl.duoc.pedidos360.admin.dto.QueueRequest;
import cl.duoc.pedidos360.admin.dto.QueueResponse;
import cl.duoc.pedidos360.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queues")
public class QueueController {

    private final RabbitAdminService service;

    public QueueController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QueueResponse crear(@Valid @RequestBody QueueRequest request) {
        return service.crearCola(request);
    }

    @GetMapping("/{name}")
    public QueueResponse obtener(@PathVariable String name) {
        return service.obtenerCola(name);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String name) {
        service.eliminarCola(name);
    }
}
