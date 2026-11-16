package pe.edu.upeu.polleria.pedidos.web.repository;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.polleria.pedidos.web.entity.WebPedido;

public interface WebPedidoRepository extends JpaRepository<WebPedido, Long> {
    Optional<WebPedido> findByClaveIdempotencia(String clave);

    Optional<WebPedido> findBySesionIdAndClaveIdempotencia(String sesion, String clave);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from WebPedido p where p.pedidoId=:id")
    Optional<WebPedido> bloquear(@Param("id") Long id);
}
