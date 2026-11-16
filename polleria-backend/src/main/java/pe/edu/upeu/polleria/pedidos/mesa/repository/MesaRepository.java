package pe.edu.upeu.polleria.pedidos.mesa.repository;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import pe.edu.upeu.polleria.pedidos.mesa.entity.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id=:id")
    Optional<Mesa> bloquear(Long id);
}
