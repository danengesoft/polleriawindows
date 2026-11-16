package pe.edu.upeu.polleria.catalogo.categoria.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaRequest {
    @NotBlank
    @Size(max = 80)
    private String nombre;
    @Size(max = 500)
    private String descripcion;
    @NotNull
    private Boolean activo;
}
