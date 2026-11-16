package pe.edu.upeu.polleria.pedidos.pedido.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.polleria.pedidos.pedido.entity.TipoPedido;
import pe.edu.upeu.polleria.pedidos.pedido.entity.MetodoPago;

@Getter
@Setter
public class PedidoRequest {
    @NotNull
    @Positive
    private Long clienteId;
    @Positive
    private Long mesaId;
    @NotNull

    private TipoPedido tipoPedido;
    @NotNull
    private MetodoPago metodoPago;
    @Size(max = 300)
    private String direccionEntrega;
    @Size(max = 300)
    private String referenciaEntrega;
    @NotEmpty
    @Valid
    private List<@NotNull DetallePedidoRequest> detalles;
}
