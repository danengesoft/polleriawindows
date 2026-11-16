package pe.edu.upeu.polleria.contenido.destacados;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/destacados")
public class DestacadosController {
    public record Seleccion(@NotNull @Size(max = 3) List<@NotNull @Positive Long> productoIds, boolean configurada) {
    }

    private final DestacadosService service;

    public DestacadosController(DestacadosService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Seleccion> consultar() {
        return service.consultar();
    }

    @PutMapping
    public Seleccion guardar(@Valid @RequestBody Seleccion r) {
        return service.guardar(r);
    }
}
