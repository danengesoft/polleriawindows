package pe.edu.upeu.polleria.pedidos.pedido.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;

public record MiPedidoRequest(
        @Positive Long mesaId, @NotNull TipoPedido tipoPedido, @NotNull MetodoPago metodoPago,
        @Size(max = 300) String direccionEntrega, @Size(max = 300) String referenciaEntrega,
        @NotEmpty @Valid List<@NotNull DetallePedidoRequest> detalles) {
    public MiPedidoRequest(TipoPedido tipoPedido, MetodoPago metodoPago, String direccionEntrega,
            String referenciaEntrega, List<DetallePedidoRequest> detalles) {
        this(null, tipoPedido, metodoPago, direccionEntrega, referenciaEntrega, detalles);
    }

    public PedidoRequest toInterno(Long clienteId) {
        PedidoRequest r = new PedidoRequest();
        r.setClienteId(clienteId);
        r.setMesaId(mesaId);
        r.setTipoPedido(tipoPedido);
        r.setMetodoPago(metodoPago);
        r.setDireccionEntrega(direccionEntrega);
        r.setReferenciaEntrega(referenciaEntrega);
        r.setDetalles(detalles);
        return r;
    }
}
