package pe.edu.upeu.polleria.catalogo.producto.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PresentacionRequest(@Positive Long id, @NotBlank @Size(max = 60) String nombre,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @NotNull Boolean disponible) {
}
