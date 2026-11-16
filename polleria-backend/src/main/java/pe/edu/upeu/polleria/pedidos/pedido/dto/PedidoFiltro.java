package pe.edu.upeu.polleria.pedidos.pedido.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import pe.edu.upeu.polleria.pedidos.pedido.entity.EstadoPedido;
import pe.edu.upeu.polleria.pedidos.pedido.entity.TipoPedido;

@Getter
@Setter
public class PedidoFiltro {
    private EstadoPedido estado;
    private TipoPedido tipoPedido;
    @Positive
    private Long clienteId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

    private LocalDate fechaDesde;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

    private LocalDate fechaHasta;
}
