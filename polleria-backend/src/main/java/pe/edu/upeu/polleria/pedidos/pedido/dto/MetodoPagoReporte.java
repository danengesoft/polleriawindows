package pe.edu.upeu.polleria.pedidos.pedido.dto;

public record MetodoPagoReporte(pe.edu.upeu.polleria.pedidos.pedido.entity.MetodoPago metodoPago, long cantidadVentas,
        java.math.BigDecimal totalVentas) {
}
