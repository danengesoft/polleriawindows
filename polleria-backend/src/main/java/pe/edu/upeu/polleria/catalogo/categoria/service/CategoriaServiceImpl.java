package pe.edu.upeu.polleria.catalogo.categoria.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import pe.edu.upeu.polleria.catalogo.categoria.dto.*;
import pe.edu.upeu.polleria.catalogo.categoria.entity.Categoria;
import pe.edu.upeu.polleria.catalogo.categoria.mapper.CategoriaMapper;
import pe.edu.upeu.polleria.catalogo.categoria.repository.CategoriaRepository;
import pe.edu.upeu.polleria.exception.*;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    private final pe.edu.upeu.polleria.catalogo.producto.repository.ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().filter(c -> !Boolean.TRUE.equals(c.getEliminado()))
                .map(this::respuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return respuesta(buscarOFallar(id));
    }

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = normalizarNombre(request.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCaseAndEliminadoFalse(nombre)) {
            throw new BusinessConflictException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = categoriaMapper.toEntity(request);
        categoria.setNombre(nombre);
        return respuesta(categoriaRepository.saveAndFlush(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.bloquear(id).filter(c -> !Boolean.TRUE.equals(c.getEliminado()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
        String nombre = normalizarNombre(request.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNotAndEliminadoFalse(nombre, id)) {
            throw new BusinessConflictException("Ya existe una categoría con ese nombre");
        }
        boolean estabaActiva = Boolean.TRUE.equals(categoria.getActivo());
        categoriaMapper.actualizar(request, categoria);
        categoria.setNombre(nombre);
        if (!estabaActiva || !Boolean.TRUE.equals(categoria.getActivo())) {
            productoRepository.findByCategoriaId(id).forEach(p -> p.setDisponible(false));
        }
        return respuesta(categoriaRepository.saveAndFlush(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.bloquear(id).filter(c -> !Boolean.TRUE.equals(c.getEliminado()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
        categoria.setEliminado(true);
        categoria.setActivo(false);
        productoRepository.findByCategoriaId(id).forEach(p -> {
            p.setEliminado(true);
            p.setDisponible(false);
        });
        categoriaRepository.saveAndFlush(categoria);
    }

    private CategoriaResponse respuesta(Categoria entidad) {
        CategoriaResponse respuesta = categoriaMapper.toResponse(entidad);
        respuesta.setEliminable(true);
        return respuesta;
    }

    private Categoria buscarOFallar(Long id) {
        return categoriaRepository.findById(id).filter(c -> !Boolean.TRUE.equals(c.getEliminado()))
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + id));
    }

    private String normalizarNombre(String nombre) {
        return nombre.strip().replaceAll("\\s+", " ");
    }
}
