package pe.edu.upeu.polleria.catalogo.producto.api;

public interface ProductoConsultaService {
    ProductoParaPedido consultarParaPedido(Long id);

    default ProductoParaPedido consultarPresentacion(Long productoId, Long presentacionId) {
        return consultarParaPedido(productoId);
    }
}
