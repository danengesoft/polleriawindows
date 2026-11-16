package pe.edu.upeu.polleria.catalogo.producto.service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.polleria.exception.*;

@Service
public class ImagenProductoService {
    public static final long MAX_BYTES = 20L * 1024 * 1024;
    private final Path directorio;
    private final String basePublica;

    public ImagenProductoService(@Value("${polleria.imagenes.directorio:./var/imagenes-productos}") String carpeta,
            @Value("${polleria.imagenes.base-publica:http://localhost:8080}") String base) {
        directorio = Path.of(carpeta).toAbsolutePath().normalize();
        URI origen = URI.create(base);
        if (!Set.of("http", "https").contains(origen.getScheme()) || origen.getHost() == null
                || origen.getUserInfo() != null || origen.getQuery() != null || origen.getFragment() != null)
            throw new IllegalArgumentException("Origen público de imágenes inválido");
        basePublica = base.replaceAll("/+$", "");
    }

    public String guardar(MultipartFile archivo) {
        if (archivo.isEmpty())
            throw new InvalidRequestException("Selecciona una imagen JPG o PNG");
        if (archivo.getSize() > MAX_BYTES)
            throw new org.springframework.web.multipart.MaxUploadSizeExceededException(MAX_BYTES);
        if (!Set.of("image/jpeg", "image/png").contains(Objects.toString(archivo.getContentType(), "")))
            throw new InvalidRequestException("Solo se admiten imágenes JPG y PNG");
        try (InputStream entrada = archivo.getInputStream();
                ImageInputStream imagen = ImageIO.createImageInputStream(entrada)) {
            if (imagen == null)
                throw invalida();
            var lectores = ImageIO.getImageReaders(imagen);
            if (!lectores.hasNext())
                throw invalida();
            var lector = lectores.next();
            BufferedImage reducida;
            String formato;
            try {
                lector.setInput(imagen, true, true);
                formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("jpeg", "jpg", "png").contains(formato))
                    throw invalida();
                int ancho = lector.getWidth(0), alto = lector.getHeight(0);
                if (ancho < 1 || alto < 1 || ancho > 16000 || alto > 16000 || (long) ancho * alto > 64_000_000L)
                    throw new InvalidRequestException(
                            "La imagen supera el límite de dimensiones: 16000 px y 64 megapíxeles");
                int muestreo = Math.max(1, (int) Math.ceil(Math.max(ancho, alto) / 2400.0));
                var parametros = lector.getDefaultReadParam();
                parametros.setSourceSubsampling(muestreo, muestreo, 0, 0);
                reducida = lector.read(0, parametros);
                if (reducida == null)
                    throw invalida();
            } finally {
                lector.dispose();
            }
            // Se recodifica: no se almacenan metadatos, nombres o contenido adjunto
            // original.
            String extension = formato.equals("png") ? "png" : "jpg";
            String nombre = UUID.randomUUID() + "." + extension;
            Files.createDirectories(directorio);
            Path temporal = Files.createTempFile(directorio, "imagen-", ".tmp");
            try {
                if (!ImageIO.write(reducida, extension, temporal.toFile()))
                    throw invalida();
                Files.move(temporal, directorio.resolve(nombre), StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temporal);
            }
            return basePublica + "/api/v1/productos/imagenes/" + nombre;
        } catch (InvalidRequestException e) {
            throw e;
        } catch (IOException | IllegalArgumentException e) {
            throw invalida();
        }
    }

    public Path obtener(String nombre) {
        if (!nombre.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(jpg|png)"))
            throw new ResourceNotFoundException("Imagen no encontrada");
        Path destino = directorio.resolve(nombre).normalize();
        if (!destino.getParent().equals(directorio) || !Files.isRegularFile(destino, LinkOption.NOFOLLOW_LINKS))
            throw new ResourceNotFoundException("Imagen no encontrada");
        return destino;
    }

    private InvalidRequestException invalida() {
        return new InvalidRequestException("No se pudo procesar la imagen. Utiliza un archivo JPG o PNG válido");
    }
}
