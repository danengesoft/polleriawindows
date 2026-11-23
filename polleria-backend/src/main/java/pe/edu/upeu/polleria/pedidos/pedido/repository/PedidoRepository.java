package pe.edu.upeu.polleria.pedidos.pedido.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.polleria.pedidos.pedido.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
        List<Pedido> findByCobradoPorSubAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(String sub,
                        LocalDateTime desde, LocalDateTime hasta);

        @Override
        @EntityGraph(attributePaths = "detalles")
        List<Pedido> findAll();

        @Override
        @EntityGraph(attributePaths = "detalles")
        Optional<Pedido> findById(Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT p FROM Pedido p WHERE p.id = :id")
        Optional<Pedido> buscarParaCambioEstado(@Param("id") Long id);

        // PostgreSQL no deduce el tipo de una fecha nula en ":desde IS NULL": el CAST se lo indica.
        String FILTROS = """
                        WHERE (:estado IS NULL OR p.estado = :estado)
                          AND (:tipo IS NULL OR p.tipoPedido = :tipo)
                          AND (:clienteId IS NULL OR p.clienteId = :clienteId)
                          AND (CAST(:desde AS LocalDateTime) IS NULL OR p.fecha >= :desde)
                          AND (CAST(:hasta AS LocalDateTime) IS NULL OR p.fecha < :hasta)
                        """;
        String VENTA_EFECTIVA = """
                          AND p.estado = pe.edu.upeu.polleria.pedidos.pedido.entity.EstadoPedido.ENTREGADO
                          AND p.estadoPago = pe.edu.upeu.polleria.pedidos.pedido.entity.EstadoPago.PAGADO
                        """;

        @EntityGraph(attributePaths = "detalles")
        @Query("SELECT p FROM Pedido p " + FILTROS)
        List<Pedido> buscar(@Param("estado") EstadoPedido estado, @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta, Sort sort);

        @Query("""
                        SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.PedidoAgregado(
                            COUNT(p), COALESCE(SUM(p.total), 0BD)) FROM Pedido p
                        """ + FILTROS + VENTA_EFECTIVA)
        PedidoAgregado agregado(@Param("estado") EstadoPedido estado, @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta);

        @Query("""
                        SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.ProductoVendidoReporte(
                            d.productoId, MIN(d.nombreProducto), SUM(d.cantidad), SUM(d.subtotal))
                        FROM DetallePedido d JOIN d.pedido p
                        """ + FILTROS + VENTA_EFECTIVA + """
                        GROUP BY d.productoId
                        ORDER BY SUM(d.cantidad) DESC, SUM(d.subtotal) DESC, d.productoId ASC
                        """)
        List<ProductoVendidoReporte> productosVendidos(@Param("estado") EstadoPedido estado,
                        @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta, org.springframework.data.domain.Pageable limite);

        @Query("""
                        SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.EstadoPedidoReporte(p.estado, COUNT(p))
                        FROM Pedido p
                        """ + FILTROS + " GROUP BY p.estado")
        List<EstadoPedidoReporte> porEstado(@Param("estado") EstadoPedido estado, @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta);

        @Query("""
                        SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.TipoPedidoReporte(p.tipoPedido, COUNT(p))
                        FROM Pedido p
                        """ + FILTROS + " GROUP BY p.tipoPedido")
        List<TipoPedidoReporte> porTipo(@Param("estado") EstadoPedido estado, @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta);

        @Query("SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.VentaDiaReporte(cast(truncate(p.fecha, day) as LocalDate), COUNT(p), SUM(p.total)) FROM Pedido p "
                        + FILTROS + VENTA_EFECTIVA
                        + " GROUP BY cast(truncate(p.fecha, day) as LocalDate) ORDER BY cast(truncate(p.fecha, day) as LocalDate)")
        List<VentaDiaReporte> ventasPorDia(@Param("estado") EstadoPedido estado, @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta);

        @Query("SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.MetodoPagoReporte(p.metodoPago, COUNT(p), SUM(p.total)) FROM Pedido p "
                        + FILTROS + VENTA_EFECTIVA + " GROUP BY p.metodoPago ORDER BY p.metodoPago")
        List<MetodoPagoReporte> metodosPago(@Param("estado") EstadoPedido estado, @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta);

        @Query("SELECT new pe.edu.upeu.polleria.pedidos.pedido.dto.ClienteCompradorReporte(p.clienteId, COUNT(p), SUM(p.total)) FROM Pedido p "
                        + FILTROS + VENTA_EFECTIVA
                        + " GROUP BY p.clienteId ORDER BY COUNT(p) DESC, SUM(p.total) DESC, p.clienteId")
        List<ClienteCompradorReporte> clientesCompradores(@Param("estado") EstadoPedido estado,
                        @Param("tipo") TipoPedido tipo,
                        @Param("clienteId") Long clienteId, @Param("desde") LocalDateTime desde,
                        @Param("hasta") LocalDateTime hasta, org.springframework.data.domain.Pageable limite);

}
