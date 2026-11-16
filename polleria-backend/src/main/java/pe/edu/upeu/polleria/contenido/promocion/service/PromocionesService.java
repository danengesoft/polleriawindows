package pe.edu.upeu.polleria.contenido.promocion.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.io.IOException;
import java.math.*;
import java.util.*;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.polleria.contenido.promocion.dto.PromocionesRequest;
import pe.edu.upeu.polleria.contenido.promocion.dto.PromocionesRequest.Oferta;
import pe.edu.upeu.polleria.contenido.promocion.api.PrecioPromocionService;
import pe.edu.upeu.polleria.catalogo.producto.api.ProductoConsultaService;
import pe.edu.upeu.polleria.exception.*;

@Service
public class PromocionesService implements PrecioPromocionService {
    private final Path archivo;
    private final ObjectMapper mapper;
    private final ProductoConsultaService productos;

    public PromocionesService(@Value("${polleria.imagenes.directorio:./var/imagenes-productos}") String directorio,
            ObjectMapper mapper, ProductoConsultaService productos) {
        this.archivo = Path.of(directorio).toAbsolutePath().normalize().resolve("promociones.json");
        this.mapper = mapper;
        this.productos = productos;
    }

    private PromocionesRequest leer() {
        try {
            return Files.exists(archivo) ? mapper.readValue(Files.readAllBytes(archivo), PromocionesRequest.class)
                    : new PromocionesRequest(List.of());
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron leer las promociones", e);
        }
    }

    public synchronized PromocionesRequest consultar() {
        var r = leer();
        List<Oferta> ofertas = new ArrayList<>();
        for (var o : r.ofertas())
            try {
                var p = productos.consultarPresentacion(o.getProductoId(), o.getPresentacionId());
                if (Boolean.TRUE.equals(p.disponible()))
                    ofertas.add(completar(o, p.precio()));
            } catch (ResourceNotFoundException | InvalidRequestException e) {
                /* Producto retirado o precio que ya no admite la oferta. */}
        return new PromocionesRequest(r.productoIds(), ofertas);
    }

    private BigDecimal calcular(Oferta o, BigDecimal regular) {
        if (o.getValor() == null || o.getValor().signum() <= 0 || o.getValor().scale() > 2)
            throw new InvalidRequestException("Indica un descuento válido con hasta dos decimales");
        BigDecimal precio;
        if ("PORCENTAJE".equals(o.getTipo())) {
            if (o.getValor().compareTo(new BigDecimal("100")) >= 0)
                throw new InvalidRequestException("El porcentaje debe ser mayor que 0 y menor que 100");
            precio = regular.multiply(BigDecimal.ONE.subtract(o.getValor().movePointLeft(2))).setScale(2,
                    RoundingMode.HALF_UP);
        } else if ("PRECIO".equals(o.getTipo()))
            precio = o.getValor().setScale(2, RoundingMode.UNNECESSARY);
        else
            throw new InvalidRequestException("Tipo de descuento inválido");
        if (precio.signum() <= 0 || precio.compareTo(regular) >= 0)
            throw new InvalidRequestException("El precio promocional debe ser positivo y menor al precio regular");
        return precio;
    }

    private Oferta completar(Oferta o, BigDecimal regular) {
        var precio = calcular(o, regular);
        return new Oferta(o.getPresentacionId(), o.getId(), o.getProductoId(), o.getTipo(), o.getValor(), regular,
                precio,
                "PORCENTAJE".equals(o.getTipo()) ? o.getValor()
                        : regular.subtract(precio).multiply(new BigDecimal("100")).divide(regular, 2,
                                RoundingMode.HALF_UP));
    }

    public synchronized PromocionesRequest guardar(PromocionesRequest request) {
        var ids = request.productoIds();
        if (ids == null || ids.size() > 20 || ids.stream().anyMatch(id -> id == null || id <= 0)
                || ids.stream().distinct().count() != ids.size())
            throw new InvalidRequestException("Selecciona hasta 20 productos diferentes");
        var anteriores = leer().ofertas();
        List<Oferta> ofertas = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        for (var o : request.ofertas()) {
            if (o == null || o.getProductoId() == null || !ids.contains(o.getProductoId())
                    || !vistos.add(o.getProductoId()))
                throw new InvalidRequestException("Revisa los productos de las promociones");
            var p = productos.consultarPresentacion(o.getProductoId(), o.getPresentacionId());
            if (!Boolean.TRUE.equals(p.disponible()))
                throw new InvalidRequestException("Selecciona productos disponibles");
            completar(o, p.precio());
            var anterior = anteriores.stream()
                    .filter(a -> a.getProductoId().equals(o.getProductoId())
                            && Objects.equals(a.getPresentacionId(), o.getPresentacionId())
                            && Objects.equals(a.getTipo(), o.getTipo()) && a.getValor().compareTo(o.getValor()) == 0)
                    .findFirst();
            o.setId(anterior.map(Oferta::getId).orElseGet(() -> UUID.randomUUID().toString()));
            ofertas.add(completar(o, p.precio()));
        }
        for (Long id : ids)
            if (!vistos.contains(id) && !Boolean.TRUE.equals(productos.consultarParaPedido(id).disponible()))
                throw new InvalidRequestException("Selecciona únicamente productos disponibles de categorías activas");
        if (!request.ofertas().isEmpty() && vistos.size() != ids.size())
            throw new InvalidRequestException("Configura el descuento de cada producto seleccionado");
        var limpio = new PromocionesRequest(List.copyOf(ids), ofertas);
        try {
            Files.createDirectories(archivo.getParent());
            Path temporal = Files.createTempFile(archivo.getParent(), "promociones-", ".tmp");
            try {
                Files.write(temporal, mapper.writeValueAsBytes(limpio));
                if (Files.exists(archivo))
                    Files.copy(archivo, archivo.resolveSibling("promociones.anterior.json"),
                            StandardCopyOption.REPLACE_EXISTING);
                Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporal);
            }
            return limpio;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar las promociones", e);
        }
    }

    @Override
    public synchronized BigDecimal precio(Long productoId, String promocionId, BigDecimal regular) {
        return precioPresentacion(productoId, productos.consultarParaPedido(productoId).presentacionId(), promocionId,
                regular);
    }

    @Override
    public synchronized BigDecimal precioPresentacion(Long productoId, Long presentacionId, String promocionId,
            BigDecimal regular) {
        var o = leer().ofertas().stream()
                .filter(a -> Objects.equals(a.getProductoId(), productoId) && Objects.equals(a.getId(), promocionId))
                .findFirst().orElseThrow(() -> new BusinessConflictException(
                        "La promoción cambió o ya no está disponible. Retira la oferta del carrito y vuelve a seleccionarla."));
        Long objetivo = o.getPresentacionId() != null ? o.getPresentacionId()
                : productos.consultarParaPedido(productoId).presentacionId();
        if (!Objects.equals(objetivo, presentacionId))
            throw new BusinessConflictException("La promoción corresponde a otra presentación");
        return calcular(o, regular);
    }
}
