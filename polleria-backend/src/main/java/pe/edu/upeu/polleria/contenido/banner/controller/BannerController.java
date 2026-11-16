package pe.edu.upeu.polleria.contenido.banner.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.polleria.contenido.banner.dto.*;
import pe.edu.upeu.polleria.contenido.banner.service.BannerService;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor

public class BannerController {
    private final BannerService service;

    @GetMapping
    public ResponseEntity<BannersRequest> publico() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
                new BannersRequest(service.consultar().banners().stream().filter(b -> Boolean.TRUE.equals(b.activo()))
                        .map(b -> new Banner(b.id(), b.titulo(), b.imagenUrl(), null, b.activo())).toList()));
    }

    @GetMapping("/gestion")
    public BannersRequest gestion() {
        return service.consultar();
    }

    @PutMapping
    public BannersRequest guardar(@Valid @RequestBody BannersRequest request) {
        return service.guardar(request);
    }
}
