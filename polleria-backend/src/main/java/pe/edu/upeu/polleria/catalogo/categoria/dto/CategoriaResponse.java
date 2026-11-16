package pe.edu.upeu.polleria.catalogo.categoria.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponse {
    private Long id;
    private Boolean eliminable;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
