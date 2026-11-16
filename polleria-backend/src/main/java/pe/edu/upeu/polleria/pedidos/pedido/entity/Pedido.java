package pe.edu.upeu.polleria.pedidos.pedido.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PEDIDOS", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "CLIENTE_ID", nullable = false)
    private Long clienteId;
    @Column(name = "FLUJO_VERSION", nullable = false)
    private Integer flujoVersion = 2;
    @Column(name = "OCUPACION_MESA_ID")
    private Long ocupacionMesaId;
    @Column(name = "CREADO_POR_SUB", length = 255)
    private String creadoPorSub;
    @Column(name = "COBRADO_POR_SUB", length = 255)
    private String cobradoPorSub;
    @Column(name = "FECHA_PAGO")
    private LocalDateTime fechaPago;
    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_PEDIDO", nullable = false, length = 15)
    private TipoPedido tipoPedido;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoPedido estado;
    @Enumerated(EnumType.STRING)
    @Column(name = "METODO_PAGO", nullable = false, length = 15)
    private MetodoPago metodoPago;
    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_PAGO", nullable = false, length = 15)
    private EstadoPago estadoPago;
    @Column(name = "DIRECCION_ENTREGA", length = 300)
    private String direccionEntrega;
    @Column(name = "REFERENCIA_ENTREGA", length = 300)
    private String referenciaEntrega;
    @Column(name = "TOTAL", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DetallePedido> detalles = new ArrayList<>();
}
