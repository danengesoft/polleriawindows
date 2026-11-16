package pe.edu.upeu.polleria.clientes.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.polleria.clientes.cliente.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    @Query(value = "SELECT (SELECT COUNT(*) FROM POLLERIA.PEDIDOS WHERE CLIENTE_ID = :id) + (SELECT COUNT(*) FROM POLLERIA.OCUPACIONES_MESA WHERE CLIENTE_ID = :id) FROM DUAL", nativeQuery = true)
    long contarReferencias(@Param("id") Long id);

    java.util.Optional<Cliente> findByWebSesionIdAndWebHuella(String sesion, String huella);

    java.util.Optional<Cliente> findByKeycloakSub(String sub);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE LOWER(TRIM(c.correo)) = :correo")
    long contarPorCorreo(@Param("correo") String correo);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE LOWER(TRIM(c.correo)) = :correo AND c.id <> :id")
    long contarPorCorreoExcluyendoId(@Param("correo") String correo, @Param("id") Long id);
}
