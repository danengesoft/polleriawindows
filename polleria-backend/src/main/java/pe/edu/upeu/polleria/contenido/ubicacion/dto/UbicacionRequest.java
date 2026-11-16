package pe.edu.upeu.polleria.contenido.ubicacion.dto;

import jakarta.validation.constraints.*;

public record UbicacionRequest(@NotNull @Size(max = 300) String direccion,
        @NotNull @Size(max = 2048) String googleMapsUrl) {
}
