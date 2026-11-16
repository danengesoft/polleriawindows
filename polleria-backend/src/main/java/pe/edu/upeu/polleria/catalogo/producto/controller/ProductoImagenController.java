package pe.edu.upeu.polleria.catalogo.producto.controller;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.polleria.catalogo.producto.service.ImagenProductoService;

@RestController
@RequestMapping("/api/v1/productos/imagenes")
@RequiredArgsConstructor

public class ProductoImagenController {
    private final ImagenProductoService imagenes;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> cargar(@RequestParam("archivo") MultipartFile archivo) {
        return Map.of("imagenUrl", imagenes.guardar(archivo));
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<FileSystemResource> obtener(@PathVariable String nombre) {
        var archivo = imagenes.obtener(nombre);
        return ResponseEntity.ok().contentType(nombre.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .body(new FileSystemResource(archivo));
    }
}
