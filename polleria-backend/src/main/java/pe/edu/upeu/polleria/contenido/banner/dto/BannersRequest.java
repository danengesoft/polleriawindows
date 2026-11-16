package pe.edu.upeu.polleria.contenido.banner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record BannersRequest(@NotNull @Size(max = 10) List<@NotNull @Valid Banner> banners) {
}
