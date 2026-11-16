package pe.edu.upeu.polleria.catalogo.producto.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.catalogo.producto.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCategoriaId(Long categoriaId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM POLLERIA.DETALLE_PEDIDOS WHERE PRODUCTO_ID = :id", nativeQuery = true)
    long contarReferencias(@org.springframework.data.repository.query.Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Producto> findAll();

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findById(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByCategoriaId(Long categoriaId);
}
