package pe.edu.upeu.polleria.pedidos.web.repository;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.polleria.pedidos.web.entity.WebSesion;

public interface WebSesionRepository extends JpaRepository<WebSesion, String> {
    Optional<WebSesion> findByTokenHash(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from WebSesion s where s.tokenHash=:hash")
    Optional<WebSesion> bloquear(@Param("hash") String hash);
}
