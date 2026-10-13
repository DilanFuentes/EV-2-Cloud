package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.dto.ClienteDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/clientes")
public class ClienteBffController {

    private final RestTemplate restTemplate;

    @Value("${services.ms-clientes.url}")
    private String msClientesUrl;

    public ClienteBffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<ClienteDTO[]> listar() {
        return ResponseEntity.ok(restTemplate.getForObject(msClientesUrl + "/clientes", ClienteDTO[].class));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(restTemplate.getForObject(msClientesUrl + "/clientes/" + id, ClienteDTO.class));
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@RequestBody ClienteDTO cliente) {
        return ResponseEntity.ok(restTemplate.postForObject(msClientesUrl + "/clientes", cliente, ClienteDTO.class));
    }
}
