package pe.edu.upeu.polleria.clientes.cliente.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.polleria.clientes.cliente.dto.*;
import pe.edu.upeu.polleria.clientes.cliente.service.ClienteService;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService service;

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        validarCorreo(request.getCorreo(), null);
        ClienteResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        validarCorreo(request.getCorreo(), service.obtener(id).getCorreo());
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private void validarCorreo(String correo, String anterior) {
        if (correo == null || correo.isBlank() || correo.equalsIgnoreCase(anterior))
            return;
        if (!correo.matches("(?i)[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@gmail\\.com"))
            throw new pe.edu.upeu.polleria.exception.InvalidRequestException("Utiliza un correo @gmail.com");
    }

}
