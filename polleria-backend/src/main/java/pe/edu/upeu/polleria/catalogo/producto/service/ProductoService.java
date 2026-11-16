package pe.edu.upeu.polleria.catalogo.producto.service;

import java.util.List;
import pe.edu.upeu.polleria.catalogo.producto.dto.ProductoRequest;
import pe.edu.upeu.polleria.catalogo.producto.dto.ProductoResponse;

public interface ProductoService {
    List<ProductoResponse> listar();

    ProductoResponse obtener(Long id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    void eliminar(Long id);

    List<ProductoResponse> listarPorCategoria(Long categoriaId);
}
