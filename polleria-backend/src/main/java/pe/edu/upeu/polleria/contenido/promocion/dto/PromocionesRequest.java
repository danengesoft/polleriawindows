package pe.edu.upeu.polleria.contenido.promocion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PromocionesRequest {
    @NotNull
    @Size(max = 20)
    private List<@NotNull @Positive Long> productoIds = List.of();
    @Size(max = 20)
    @Valid
    private List<@NotNull @Valid Oferta> ofertas = List.of();

    public PromocionesRequest(List<Long> ids) {
        this.productoIds = ids;
    }

    public PromocionesRequest(List<Long> ids, List<Oferta> ofertas) {
        this.productoIds = ids;
        this.ofertas = ofertas;
    }

    public List<Long> productoIds() {
        return productoIds;
    }

    public List<Oferta> ofertas() {
        return ofertas == null ? List.of() : ofertas;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Oferta {
        @Positive
        private Long presentacionId;
        private String id;
        @NotNull
        @Positive
        private Long productoId;
        @NotNull
        private String tipo;
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 8, fraction = 2)
        private BigDecimal valor;
        private BigDecimal precioRegular;
        private BigDecimal precioPromocional;
        private BigDecimal porcentaje;

        public Oferta(String id, Long productoId, String tipo, BigDecimal valor, BigDecimal precioRegular,
                BigDecimal precioPromocional, BigDecimal porcentaje) {
            this(null, id, productoId, tipo, valor, precioRegular, precioPromocional, porcentaje);
        }
    }
}
