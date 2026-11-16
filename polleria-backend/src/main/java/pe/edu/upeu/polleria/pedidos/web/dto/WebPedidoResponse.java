package pe.edu.upeu.polleria.pedidos.web.dto;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;

/**
 * Vista de posesión: no expone comprador, teléfono, dirección, ocupación ni
 * identidades.
 */
public record WebPedidoResponse(Long id, Integer flujoVersion, LocalDateTime fecha, TipoPedido tipoPedido,
        EstadoPedido estado, EstadoPago estadoPago, MetodoPago metodoPago, BigDecimal total, String mesaNumero,
        List<DetallePedidoResponse> detalles) {
    public static WebPedidoResponse de(PedidoResponse p) {
        return new WebPedidoResponse(p.getId(), p.getFlujoVersion(), p.getFecha(), p.getTipoPedido(), p.getEstado(),
                p.getEstadoPago(), p.getMetodoPago(), p.getTotal(), p.getMesaNumero(), p.getDetalles());
    }
}
