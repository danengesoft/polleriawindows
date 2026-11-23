package pe.edu.upeu.polleria.clinica.clinica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.service.ClinicaService;

@RestController
@RequestMapping("/api/v1/clinica/clinicas")
@RequiredArgsConstructor
public class ClinicaController {

    private final ClinicaService service;

    @GetMapping
    public ResponseEntity<List<ClinicaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ClinicaResponse> crear(@Valid @RequestBody ClinicaRequest request) {
        ClinicaResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/clinica/clinicas/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ClinicaRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
