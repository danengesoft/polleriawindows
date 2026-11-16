package pe.edu.upeu.polleria.catalogo.categoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.catalogo.categoria.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Categoria c where c.id = :id")
    java.util.Optional<Categoria> bloquear(@org.springframework.data.repository.query.Param("id") Long id);

    boolean existsByNombreIgnoreCaseAndEliminadoFalse(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse(String nombre, Long id);
}
