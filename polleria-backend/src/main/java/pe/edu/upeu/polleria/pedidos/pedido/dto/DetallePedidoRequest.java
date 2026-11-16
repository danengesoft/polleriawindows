package pe.edu.upeu.polleria.pedidos.pedido.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetallePedidoRequest {
    @NotNull
    @Positive
    private Long productoId;
    @Positive
    private Long presentacionId;
    @Pattern(regexp = "[a-fA-F0-9-]{36}")
    private String promocionId;
    @NotNull
    @Positive
    private Integer cantidad;
}
