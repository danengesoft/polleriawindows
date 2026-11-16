package pe.edu.upeu.polleria.pedidos.mesa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import pe.edu.upeu.polleria.pedidos.mesa.entity.OcupacionMesa;

public interface OcupacionMesaRepository extends JpaRepository<OcupacionMesa, Long> {
    Optional<OcupacionMesa> findByMesaIdAndFechaLiberacionIsNull(Long mesaId);
}
