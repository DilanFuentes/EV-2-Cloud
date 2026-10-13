package cl.duoc.pedidos360.admin.controller;

import cl.duoc.pedidos360.admin.dto.ExchangeRequest;
import cl.duoc.pedidos360.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exchanges")
public class ExchangeController {

    private final RabbitAdminService service;

    public ExchangeController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRequest crear(@Valid @RequestBody ExchangeRequest request) {
        service.crearExchange(request);
        return request;
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String name) {
        service.eliminarExchange(name);
    }
}
