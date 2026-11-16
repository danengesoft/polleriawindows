package pe.edu.upeu.polleria.contenido.banner.dto;

import jakarta.validation.constraints.*;

public record Banner(
        @NotBlank @Pattern(regexp = "[a-f0-9-]{36}") String id,
        @NotBlank @Size(max = 120) String titulo,
        @NotBlank @Size(max = 1000) String imagenUrl,
        @Size(max = 1000) String imagenMovilUrl,
        @NotNull Boolean activo) {
}
