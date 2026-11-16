package pe.edu.upeu.polleria.catalogo.producto.api;

import java.math.BigDecimal;

public record ProductoParaPedido(Long id, String nombre, BigDecimal precio, Boolean disponible, Long presentacionId,
        String nombrePresentacion) {
    public ProductoParaPedido(Long id, String nombre, BigDecimal precio, Boolean disponible) {
        this(id, nombre, precio, disponible, null, null);
    }
}
