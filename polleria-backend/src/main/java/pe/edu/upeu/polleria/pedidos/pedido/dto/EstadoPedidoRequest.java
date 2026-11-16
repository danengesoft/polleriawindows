package pe.edu.upeu.polleria.pedidos.pedido.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.polleria.pedidos.pedido.entity.EstadoPedido;

@Getter
@Setter
public class EstadoPedidoRequest {
    @NotNull
    private EstadoPedido estado;
}
