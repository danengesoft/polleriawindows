package pe.edu.upeu.polleria.catalogo.producto.dto;

import java.math.BigDecimal;

public record PresentacionResponse(Long id, String nombre, BigDecimal precio, Boolean disponible) {
}
