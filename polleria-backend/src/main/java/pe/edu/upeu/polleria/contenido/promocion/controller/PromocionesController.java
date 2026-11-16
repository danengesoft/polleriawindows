package pe.edu.upeu.polleria.contenido.promocion.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import jakarta.validation.Valid;
import pe.edu.upeu.polleria.contenido.promocion.dto.PromocionesRequest;
import pe.edu.upeu.polleria.contenido.promocion.service.PromocionesService;

@RestController
@RequestMapping("/api/v1/promociones")

public class PromocionesController {
    private final PromocionesService service;

    public PromocionesController(PromocionesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PromocionesRequest> consultar() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.consultar());
    }

    @PutMapping
    public PromocionesRequest guardar(@Valid @RequestBody PromocionesRequest request) {
        return service.guardar(request);
    }
}
