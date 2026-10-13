package cl.duoc.pedidos360.admin.controller;

import cl.duoc.pedidos360.admin.dto.BindingRequest;
import cl.duoc.pedidos360.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bindings")
public class BindingController {

    private final RabbitAdminService service;

    public BindingController(RabbitAdminService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BindingRequest crear(@Valid @RequestBody BindingRequest request) {
        service.crearBinding(request);
        return request;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Valid @RequestBody BindingRequest request) {
        service.eliminarBinding(request);
    }
}
