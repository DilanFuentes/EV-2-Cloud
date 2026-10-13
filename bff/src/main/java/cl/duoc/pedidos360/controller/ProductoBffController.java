package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/productos")
public class ProductoBffController {

    private final RestTemplate restTemplate;

    @Value("${services.ms-productos.url}")
    private String msProductosUrl;

    public ProductoBffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<ProductoDTO[]> listar() {
        ProductoDTO[] productos = restTemplate.getForObject(msProductosUrl + "/productos", ProductoDTO[].class);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(restTemplate.getForObject(msProductosUrl + "/productos/" + id, ProductoDTO.class));
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(@RequestBody ProductoDTO producto) {
        return ResponseEntity.ok(restTemplate.postForObject(msProductosUrl + "/productos", producto, ProductoDTO.class));
    }
}
