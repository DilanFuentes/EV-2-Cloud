package cl.duoc.pedidos360.repository;

import cl.duoc.pedidos360.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
