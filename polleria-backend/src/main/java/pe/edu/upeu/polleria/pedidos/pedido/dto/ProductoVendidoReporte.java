package pe.edu.upeu.polleria.pedidos.pedido.dto;

import java.math.BigDecimal;

public record ProductoVendidoReporte(
        Long productoId,
        String nombreProducto,
        long cantidadVendida,
        BigDecimal importeGenerado) {
}
