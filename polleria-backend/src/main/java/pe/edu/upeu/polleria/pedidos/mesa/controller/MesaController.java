package pe.edu.upeu.polleria.pedidos.mesa.controller;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upeu.polleria.pedidos.mesa.service.MesaService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mesas")
public class MesaController {
    private final MesaService service;

    public record OcupacionRequest(@NotNull @Positive Long ocupacionId) {
    }

    @GetMapping
    public List<MesaService.Vista> listar() {
        return service.listar();
    }

    @PatchMapping("/{id}/ocupar")
    public void ocupar(@PathVariable Long id, @Valid @RequestBody OcupacionRequest r) {
        service.cambiar(id, r.ocupacionId(), false);
    }

    @PatchMapping("/{id}/liberar")
    public void liberar(@PathVariable Long id, @Valid @RequestBody OcupacionRequest r) {
        service.cambiar(id, r.ocupacionId(), true);
    }
}
