package pe.edu.upeu.polleria.pedidos.web.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.edu.upeu.polleria.clientes.cliente.api.DatosCompradorWeb;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;

public record WebPedidoRequest(@NotNull @Valid DatosCompradorWeb comprador, @Positive Long mesaId,
        @NotNull TipoPedido tipoPedido, @NotNull MetodoPago metodoPago, @Size(max = 300) String direccionEntrega,
        @Size(max = 300) String referenciaEntrega, @NotEmpty @Size(max = 100) @Valid List<@NotNull Linea> detalles) {
    public record Linea(@NotNull @Positive Long productoId, @NotNull @Min(1) @Max(999) Integer cantidad,
            @Pattern(regexp = "[a-fA-F0-9-]{36}") String promocionId, @Positive Long presentacionId) {
        public Linea(Long productoId, Integer cantidad) {
            this(productoId, cantidad, null, null);
        }

        public Linea(Long productoId, Integer cantidad, String promocionId) {
            this(productoId, cantidad, promocionId, null);
        }

        @Override
        public String toString() {
            return "Linea[productoId=" + productoId + ", cantidad=" + cantidad
                    + (promocionId == null ? "" : ", promocionId=" + promocionId)
                    + (presentacionId == null ? "" : ", presentacionId=" + presentacionId) + "]";
        }
    }
}
