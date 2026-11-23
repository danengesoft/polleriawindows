package pe.edu.upeu.polleria.contenido.banner.service;

import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.polleria.contenido.banner.dto.*;
import pe.edu.upeu.polleria.exception.InvalidRequestException;

@Service
public class BannerService {
    private final Path archivo;
    private final ObjectMapper mapper;

    public BannerService(@Value("${polleria.imagenes.directorio:./var/imagenes-productos}") String directorio,
            ObjectMapper mapper) {
        this.archivo = Path.of(directorio).toAbsolutePath().normalize().resolve("banners.json");
        this.mapper = mapper;
    }

    public synchronized BannersRequest consultar() {
        if (!Files.exists(archivo))
            return new BannersRequest(List.of());
        try {
            if (Files.size(archivo) > 65536)
                throw new IllegalStateException("Configuración de portada inválida");
            return actualizarRutas(mapper.readValue(Files.readAllBytes(archivo), BannersRequest.class));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la portada", e);
        }
    }

    public synchronized BannersRequest guardar(BannersRequest request) {
        if (request.banners().isEmpty() || request.banners().stream().anyMatch(b -> !Boolean.TRUE.equals(b.activo())))
            throw new InvalidRequestException(
                    "El carrusel debe conservar fotografías activas; reemplaza la imagen para cambiarla");
        request = actualizarRutas(new BannersRequest(request.banners().stream()
                .map(b -> new Banner(b.id(), b.titulo(), b.imagenUrl(), null, b.activo())).toList()));
        Set<String> ids = new HashSet<>();
        for (Banner b : request.banners()) {
            try {
                UUID.fromString(b.id());
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Identificador de imagen inválido");
            }
            if (!ids.add(b.id()))
                throw new InvalidRequestException("No repitas una imagen en la configuración");
            validarImagen(b.imagenUrl());
            if (b.imagenMovilUrl() != null && !b.imagenMovilUrl().isBlank())
                validarImagen(b.imagenMovilUrl());
        }
        try {
            Files.createDirectories(archivo.getParent());
            Path temporal = Files.createTempFile(archivo.getParent(), "portada-", ".tmp");
            try {
                Files.write(temporal, mapper.writeValueAsBytes(request));
                if (Files.exists(archivo))
                    Files.copy(archivo, archivo.resolveSibling("banners.anterior.json"),
                            StandardCopyOption.REPLACE_EXISTING);
                Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporal);
            }
            return request;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la portada", e);
        }
    }

    // Portadas guardadas antes del cambio de nombre apuntaban a /images/la-brava/.
    private static final String RUTA_ANTERIOR = "/images/la-brava/";
    private static final String FOTO_ORIGINAL = "/images/polleria-neyos/inicio-hero.png";

    private static BannersRequest actualizarRutas(BannersRequest request) {
        if (request.banners() == null)
            return request;
        return new BannersRequest(request.banners().stream().map(b -> new Banner(b.id(), b.titulo(),
                ruta(b.imagenUrl()), ruta(b.imagenMovilUrl()), b.activo())).toList());
    }

    private static String ruta(String valor) {
        return valor != null && valor.startsWith(RUTA_ANTERIOR)
                ? "/images/polleria-neyos/" + valor.substring(RUTA_ANTERIOR.length())
                : valor;
    }

    private void validarImagen(String ruta) {
        if (FOTO_ORIGINAL.equals(ruta))
            return;
        if (ruta == null || !ruta.matches(
                "/api/v1/productos/imagenes/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|png)"))
            throw new InvalidRequestException("Carga una fotografía del sistema para la portada");
        Path imagen = archivo.getParent().resolve(ruta.substring(ruta.lastIndexOf('/') + 1));
        if (!Files.isRegularFile(imagen, LinkOption.NOFOLLOW_LINKS))
            throw new InvalidRequestException("La fotografía seleccionada no existe");
    }
}
