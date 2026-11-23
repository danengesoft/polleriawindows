package pe.edu.upeu.polleria.clinica.consultorio.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioRequest;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;
import pe.edu.upeu.polleria.clinica.consultorio.service.ConsultorioService;

@RestController
@RequestMapping("/api/v1/clinica/consultorios")
@RequiredArgsConstructor
public class ConsultorioController {

    private final ConsultorioService service;

    @GetMapping
    public ResponseEntity<List<ConsultorioResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultorioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ConsultorioResponse> crear(@Valid @RequestBody ConsultorioRequest request) {
        ConsultorioResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/clinica/consultorios/" + response.getId())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
