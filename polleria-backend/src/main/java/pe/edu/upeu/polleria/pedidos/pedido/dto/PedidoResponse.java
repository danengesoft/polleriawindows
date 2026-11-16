package pe.edu.upeu.polleria.pedidos.pedido.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponse {
    private Long id;
    private Integer flujoVersion;
    private Long ocupacionMesaId;
    private Long mesaId;
    private String mesaNumero;
    private ClienteResumen cliente;
    private LocalDateTime fecha;
    private TipoPedido tipoPedido;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String direccionEntrega;
    private String referenciaEntrega;
    private BigDecimal total;
    private List<DetallePedidoResponse> detalles;
}
