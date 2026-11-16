package pe.edu.upeu.polleria.clientes.cliente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import pe.edu.upeu.polleria.clientes.cliente.dto.*;
import pe.edu.upeu.polleria.clientes.cliente.service.ClienteService;

@RestController
@RequestMapping("/api/v1/clientes/me")
@RequiredArgsConstructor
public class MiPerfilController {
    private final ClienteService service;

    @GetMapping
    public ClienteResponse obtener(@AuthenticationPrincipal Jwt jwt) {
        return service.miPerfil(jwt.getSubject());
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.created(URI.create("/api/v1/clientes/me"))
                .body(service.crearPerfil(jwt.getSubject(), request));
    }

    @PutMapping
    public ClienteResponse actualizar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest request) {
        return service.actualizarPerfil(jwt.getSubject(), request);
    }
}
