package pe.edu.upeu.polleria.pedidos.pedido.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DETALLE_PEDIDOS", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    private Pedido pedido;
    @Column(name = "PRODUCTO_ID", nullable = false)
    private Long productoId;
    @Column(name = "NOMBRE_PRODUCTO", nullable = false, length = 120)
    private String nombreProducto;
    @Column(name = "PRECIO_UNITARIO", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    @Column(name="PRESENTACION_ID")
    private Long presentacionId;
    @Column(name="NOMBRE_PRESENTACION",length=60)
    private String nombrePresentacion;
    @Column(name="PROMOCION_ID",length=36)
    private String promocionId;
    @Column(name="PRECIO_REGULAR",precision=10,scale=2)
    private BigDecimal precioRegular;
    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;
    @Column(name = "SUBTOTAL", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;
}
