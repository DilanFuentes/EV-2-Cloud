package cl.duoc.pedidos360.controller;

import cl.duoc.pedidos360.dto.PedidoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * El BFF actua como intermediario: el frontend Angular solo conoce esta URL.
 * Aqui la peticion YA paso por el filtro de seguridad (SecurityConfig) que valido el JWT,
 * por lo tanto si llegamos a este metodo, el token es valido.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoBffController {

    private final RestTemplate restTemplate;

    @Value("${services.ms-pedidos.url}")
    private String msPedidosUrl;

    public PedidoBffController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<PedidoDTO[]> listar(@AuthenticationPrincipal Jwt jwt) {
        PedidoDTO[] pedidos = restTemplate.getForObject(msPedidosUrl + "/pedidos", PedidoDTO[].class);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> obtener(@PathVariable Long id) {
        PedidoDTO pedido = restTemplate.getForObject(msPedidosUrl + "/pedidos/" + id, PedidoDTO.class);
        return ResponseEntity.ok(pedido);
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crear(@RequestBody PedidoDTO pedido) {
        PedidoDTO creado = restTemplate.postForObject(msPedidosUrl + "/pedidos", pedido, PedidoDTO.class);
        return ResponseEntity.ok(creado);
    }
}
