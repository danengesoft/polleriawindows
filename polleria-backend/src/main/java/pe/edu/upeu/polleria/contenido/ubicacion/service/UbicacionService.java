package pe.edu.upeu.polleria.contenido.ubicacion.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.net.URI;
import java.util.Set;
import java.io.IOException;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.polleria.contenido.ubicacion.dto.UbicacionRequest;
import pe.edu.upeu.polleria.exception.InvalidRequestException;

@Service
public class UbicacionService {
    private final Path archivo;
    private final ObjectMapper mapper;

    public UbicacionService(@Value("${polleria.imagenes.directorio:./var/imagenes-productos}") String directorio,
            ObjectMapper mapper) {
        archivo = Path.of(directorio).toAbsolutePath().normalize().resolve("ubicacion.json");
        this.mapper = mapper;
    }

    public synchronized UbicacionRequest consultar() {
        if (!Files.exists(archivo))
            return new UbicacionRequest("", "");
        try {
            if (Files.size(archivo) > 16384)
                throw new IllegalStateException("Configuración de ubicación inválida");
            return mapper.readValue(Files.readAllBytes(archivo), UbicacionRequest.class);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la ubicación", e);
        }
    }

    public synchronized UbicacionRequest guardar(UbicacionRequest r) {
        String url = r.googleMapsUrl().strip();
        if (!url.isEmpty())
            try {
                URI u = URI.create(url);
                String host = u.getHost();
                boolean corto = Set.of("maps.app.goo.gl", "maps.google.com", "maps.google.com.pe")
                        .contains(host == null ? "" : host.toLowerCase(java.util.Locale.ROOT));
                boolean largo = Set.of("google.com", "www.google.com", "google.com.pe", "www.google.com.pe", "goo.gl")
                        .contains(host == null ? "" : host.toLowerCase(java.util.Locale.ROOT))
                        && (u.getPath().equals("/maps") || u.getPath().startsWith("/maps/"));
                String path = u.getPath();
                if (!u.normalize().equals(u) || (path != null && (path.contains("/../") || path.endsWith("/..")
                        || path.contains("/./") || path.contains("\\"))))
                    throw new IllegalArgumentException();
                if (!"https".equalsIgnoreCase(u.getScheme()) || u.getUserInfo() != null
                        || (u.getPort() != -1 && u.getPort() != 443) || (!corto && !largo))
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Utiliza un enlace HTTPS válido de Google Maps");
            }
        UbicacionRequest limpio = new UbicacionRequest(r.direccion().strip(), url);
        try {
            Files.createDirectories(archivo.getParent());
            Path temp = Files.createTempFile(archivo.getParent(), "ubicacion-", ".tmp");
            try {
                Files.write(temp, mapper.writeValueAsBytes(limpio));
                if (Files.exists(archivo))
                    Files.copy(archivo, archivo.resolveSibling("ubicacion.anterior.json"),
                            StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
            return limpio;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la ubicación", e);
        }
    }
}
