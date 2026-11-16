package pe.edu.upeu.polleria.contenido.ubicacion.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.polleria.contenido.ubicacion.dto.UbicacionRequest;
import pe.edu.upeu.polleria.contenido.ubicacion.service.UbicacionService;

@RestController
@RequestMapping("/api/v1/ubicacion")
@RequiredArgsConstructor

public class UbicacionController {
    private final UbicacionService service;

    @GetMapping
    public ResponseEntity<UbicacionRequest> consultar() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.consultar());
    }

    @PutMapping
    public UbicacionRequest guardar(@Valid @RequestBody UbicacionRequest r) {
        return service.guardar(r);
    }
}
