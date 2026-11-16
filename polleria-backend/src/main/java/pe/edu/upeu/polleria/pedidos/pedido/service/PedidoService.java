package pe.edu.upeu.polleria.pedidos.pedido.service;

import java.util.List;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;

public interface PedidoService {
    List<PedidoResponse> listar();

    PedidoResponse confirmarPago(Long id);

    java.util.Map<String, Object> pagoPruebaInterno(Long id, String resultado);

    PedidoResponse crearMio(String sub, MiPedidoRequest request);

    List<PedidoResponse> listarMios(String sub);

    PedidoResponse obtenerMio(String sub, Long id);

    PedidoResponse cancelarMio(String sub, Long id);

    List<PedidoResponse> buscar(PedidoFiltro filtro, String ordenarPor, String direccion);

    PedidoResumenReporte resumen(PedidoFiltro filtro);

    List<ProductoVendidoReporte> productosMasVendidos(PedidoFiltro filtro);

    List<EstadoPedidoReporte> porEstado(PedidoFiltro filtro);

    List<TipoPedidoReporte> porTipo(PedidoFiltro filtro);

    PedidoResponse obtener(Long id);

    PedidoResponse crear(PedidoRequest request);

    PedidoResponse cambiarEstado(Long id, EstadoPedidoRequest request);

    List<ProductoVendidoReporte> productosMasVendidos(PedidoFiltro filtro, int top);

    List<VentaDiaReporte> ventasPorDia(PedidoFiltro filtro);

    List<MetodoPagoReporte> metodosPago(PedidoFiltro filtro);

    List<ClienteCompradorReporte> clientesCompradores(PedidoFiltro filtro, int top);

}
