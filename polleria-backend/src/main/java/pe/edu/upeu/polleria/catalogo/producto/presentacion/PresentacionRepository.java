package pe.edu.upeu.polleria.catalogo.producto.presentacion;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresentacionRepository extends JpaRepository<Presentacion, Long> {
    List<Presentacion> findByProductoIdAndEliminadoFalseOrderById(Long productoId);
}
