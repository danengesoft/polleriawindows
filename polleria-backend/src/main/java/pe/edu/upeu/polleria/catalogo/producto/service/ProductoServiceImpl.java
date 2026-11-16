package pe.edu.upeu.polleria.catalogo.producto.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.catalogo.categoria.entity.Categoria;
import pe.edu.upeu.polleria.catalogo.categoria.repository.CategoriaRepository;
import pe.edu.upeu.polleria.catalogo.producto.dto.*;
import pe.edu.upeu.polleria.catalogo.producto.entity.Producto;
import pe.edu.upeu.polleria.catalogo.producto.mapper.ProductoMapper;
import pe.edu.upeu.polleria.catalogo.producto.repository.ProductoRepository;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl
        implements ProductoService, pe.edu.upeu.polleria.catalogo.producto.api.ProductoConsultaService {
    private final pe.edu.upeu.polleria.catalogo.producto.presentacion.PresentacionRepository presentaciones;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream().filter(this::vigente).map(this::respuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorCategoria(Long categoriaId) {
        buscarCategoriaOFallar(categoriaId);
        return productoRepository.findByCategoriaId(categoriaId).stream().filter(this::vigente).map(this::respuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return respuesta(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = categoriaRepository.bloquear(request.getCategoriaId())
                .filter(c -> !Boolean.TRUE.equals(c.getEliminado())).orElseThrow(
                        () -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoriaId()));
        Producto producto = productoMapper.toEntity(request, categoria);
        producto.setNombre(request.getNombre().strip());
        if (!Boolean.TRUE.equals(categoria.getActivo()))
            producto.setDisponible(false);
        productoRepository.saveAndFlush(producto);
        guardarPresentaciones(producto, request);
        return respuesta(productoRepository.saveAndFlush(producto));
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarOFallar(id);
        Categoria categoria = categoriaRepository.bloquear(request.getCategoriaId())
                .filter(c -> !Boolean.TRUE.equals(c.getEliminado())).orElseThrow(
                        () -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoriaId()));
        productoMapper.actualizar(request, categoria, producto);
        producto.setNombre(request.getNombre().strip());
        if (!Boolean.TRUE.equals(categoria.getActivo()))
            producto.setDisponible(false);
        productoRepository.saveAndFlush(producto);
        guardarPresentaciones(producto, request);
        return respuesta(productoRepository.saveAndFlush(producto));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto p = buscarOFallar(id);
        p.setEliminado(true);
        p.setDisponible(false);
        productoRepository.saveAndFlush(p);
    }

    @Override
    @Transactional(readOnly = true)
    public pe.edu.upeu.polleria.catalogo.producto.api.ProductoParaPedido consultarParaPedido(Long id) {
        return consultarPresentacion(id, null);
    }

    @Override
    @Transactional(readOnly = true)
    public pe.edu.upeu.polleria.catalogo.producto.api.ProductoParaPedido consultarPresentacion(Long id,
            Long presentacionId) {
        Producto producto = buscarOFallar(id);
        var lista = presentaciones.findByProductoIdAndEliminadoFalseOrderById(id);
        if (lista.isEmpty())
            throw new pe.edu.upeu.polleria.exception.BusinessConflictException(
                    "El producto no tiene presentaciones disponibles");
        if (presentacionId == null && lista.size() != 1)
            throw new pe.edu.upeu.polleria.exception.InvalidRequestException("Selecciona la presentación del producto");
        var seleccion = lista.stream().filter(p -> presentacionId == null || p.getId().equals(presentacionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Presentación no encontrada para este producto"));
        return new pe.edu.upeu.polleria.catalogo.producto.api.ProductoParaPedido(
                producto.getId(), producto.getNombre(), seleccion.getPrecio(),
                Boolean.TRUE.equals(seleccion.getDisponible()) && Boolean.TRUE.equals(producto.getDisponible())
                        && Boolean.TRUE.equals(producto.getCategoria().getActivo()),
                seleccion.getId(), seleccion.getNombre());
    }

    private ProductoResponse respuesta(Producto entidad) {
        ProductoResponse respuesta = productoMapper.toResponse(entidad);
        var opciones = presentaciones.findByProductoIdAndEliminadoFalseOrderById(entidad.getId());
        respuesta.setDisponible(
                Boolean.TRUE.equals(entidad.getDisponible()) && Boolean.TRUE.equals(entidad.getCategoria().getActivo())
                        && opciones.stream().anyMatch(p -> Boolean.TRUE.equals(p.getDisponible())));
        respuesta.setPresentaciones(opciones.stream()
                .map(p -> new PresentacionResponse(p.getId(), p.getNombre(), p.getPrecio(), p.getDisponible()))
                .toList());
        respuesta.setPrecio(opciones.stream().filter(p -> Boolean.TRUE.equals(p.getDisponible()))
                .map(p -> p.getPrecio()).min(java.math.BigDecimal::compareTo).orElse(entidad.getPrecio()));
        respuesta.setEliminable(true);
        return respuesta;
    }

    private void guardarPresentaciones(Producto producto, ProductoRequest request) {
        var actuales = presentaciones.findByProductoIdAndEliminadoFalseOrderById(producto.getId());
        var recibidas = request.getPresentaciones();
        if (recibidas == null) {
            if (actuales.isEmpty())
                recibidas = java.util.List.of(new PresentacionRequest(null, "Única", request.getPrecio(), true));
            else if (actuales.size() == 1 && actuales.getFirst().getNombre().equals("Única"))
                recibidas = java.util.List
                        .of(new PresentacionRequest(actuales.getFirst().getId(), "Única", request.getPrecio(), true));
            else {
                producto.setPrecio(actuales.stream().map(p -> p.getPrecio()).min(java.math.BigDecimal::compareTo)
                        .orElse(request.getPrecio()));
                return;
            }
        }
        var nombres = new java.util.HashSet<String>();
        var ids = new java.util.HashSet<Long>();
        for (var r : recibidas) {
            if (!nombres.add(r.nombre().strip().replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT)))
                throw new pe.edu.upeu.polleria.exception.BusinessConflictException(
                        "No repitas nombres de presentaciones");
            if (r.id() != null && (!ids.add(r.id()) || actuales.stream().noneMatch(a -> a.getId().equals(r.id()))))
                throw new ResourceNotFoundException("La presentación no pertenece al producto");
        }
        for (var a : actuales) {
            a.setEliminado(true);
            a.setDisponible(false);
        }
        presentaciones.flush();
        for (var r : recibidas) {
            var p = r.id() == null ? new pe.edu.upeu.polleria.catalogo.producto.presentacion.Presentacion()
                    : actuales.stream().filter(a -> a.getId().equals(r.id())).findFirst().orElseThrow();
            p.setEliminado(false);
            p.setProductoId(producto.getId());
            p.setNombre(r.nombre().strip().replaceAll("\\s+", " "));
            p.setPrecio(r.precio());
            p.setDisponible(r.disponible());
            presentaciones.save(p);
        }
        presentaciones.flush();
        producto.setPrecio(
                recibidas.stream().map(PresentacionRequest::precio).min(java.math.BigDecimal::compareTo).orElseThrow());
    }

    private boolean vigente(Producto p) {
        return !Boolean.TRUE.equals(p.getEliminado()) && !Boolean.TRUE.equals(p.getCategoria().getEliminado());
    }

    private Producto buscarOFallar(Long id) {
        return productoRepository.findById(id).filter(this::vigente)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
    }

    private Categoria buscarCategoriaOFallar(Long id) {
        return categoriaRepository.findById(id).filter(c -> !Boolean.TRUE.equals(c.getEliminado()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
    }
}
