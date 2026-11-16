package pe.edu.upeu.polleria.pedidos.pedido.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;
import pe.edu.upeu.polleria.pedidos.pedido.service.PedidoService;

@RestController
@RequestMapping("/api/v1/pedidos/mios")
@RequiredArgsConstructor
public class MisPedidosController {
    private final PedidoService service;

    @GetMapping
    public List<PedidoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listarMios(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.obtenerMio(jwt.getSubject(), id);
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MiPedidoRequest request) {
        PedidoResponse r = service.crearMio(jwt.getSubject(), request);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/mios/" + r.getId())).body(r);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.cancelarMio(jwt.getSubject(), id);
    }
}
