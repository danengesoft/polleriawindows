package pe.edu.upeu.polleria.contenido.destacados;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.polleria.exception.InvalidRequestException;

import org.springframework.stereotype.Service;
import pe.edu.upeu.polleria.contenido.destacados.DestacadosController.Seleccion;

@Service
public class DestacadosService {
    private final Path archivo;
    private final ObjectMapper mapper;

    public DestacadosService(@Value("${polleria.imagenes.directorio:./var/imagenes-productos}") String directorio,
            ObjectMapper mapper) {
        this.archivo = Path.of(directorio).toAbsolutePath().normalize().resolve("destacados.json");
        this.mapper = mapper;
    }

    public synchronized ResponseEntity<Seleccion> consultar() {
        try {
            return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                    .body(Files.exists(archivo) ? mapper.readValue(Files.readAllBytes(archivo), Seleccion.class)
                            : new Seleccion(List.of(), false));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la selección de destacados", e);
        }
    }

    public synchronized Seleccion guardar(@Valid @RequestBody Seleccion r) {
        if (r.productoIds().stream().distinct().count() != r.productoIds().size())
            throw new InvalidRequestException("No repitas productos destacados");
        r = new Seleccion(List.copyOf(r.productoIds()), true);
        try {
            Files.createDirectories(archivo.getParent());
            Path temp = Files.createTempFile(archivo.getParent(), "destacados-", ".tmp");
            try {
                Files.write(temp, mapper.writeValueAsBytes(r));
                if (Files.exists(archivo))
                    Files.copy(archivo, archivo.resolveSibling("destacados.anterior.json"),
                            StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
            return r;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la selección de destacados", e);
        }
    }
}
