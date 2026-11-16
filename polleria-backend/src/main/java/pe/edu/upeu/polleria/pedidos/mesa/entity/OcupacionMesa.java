package pe.edu.upeu.polleria.pedidos.mesa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "OCUPACIONES_MESA", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class OcupacionMesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MESA_ID", nullable = false)
    private Mesa mesa;
    @Column(name = "CLIENTE_ID", nullable = false)
    private Long clienteId;
    @Column(name = "FECHA_RESERVA", nullable = false)
    private LocalDateTime fechaReserva;
    @Column(name = "FECHA_LLEGADA")
    private LocalDateTime fechaLlegada;
    @Column(name = "FECHA_LIBERACION")
    private LocalDateTime fechaLiberacion;
    @Column(name = "LIBERADA_POR_SUB", length = 255)
    private String liberadaPorSub;
}
