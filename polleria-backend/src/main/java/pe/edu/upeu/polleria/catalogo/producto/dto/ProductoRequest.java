package pe.edu.upeu.polleria.catalogo.producto.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoRequest {
    @jakarta.validation.Valid
    @Size(min = 1, max = 20)
    private java.util.List<@NotNull @jakarta.validation.Valid PresentacionRequest> presentaciones;
    @NotBlank
    @Size(max = 120)
    private String nombre;
    @Size(max = 500)
    private String descripcion;
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private java.math.BigDecimal precio;
    @NotNull
    private Boolean disponible;
    @NotNull
    @Positive
    private Long categoriaId;
    @Size(max = 1000)
    @Pattern(regexp = "(?i)^https?://[^\\s]+$")
    private String imagenUrl;
}
