package pe.edu.upeu.polleria.catalogo.producto.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private java.util.List<PresentacionResponse> presentaciones;
    private Long id;
    private Boolean eliminable;
    private String nombre;
    private String descripcion;
    private java.math.BigDecimal precio;
    private Boolean disponible;
    private pe.edu.upeu.polleria.catalogo.categoria.dto.CategoriaResumen categoria;
    private String imagenUrl;
}
