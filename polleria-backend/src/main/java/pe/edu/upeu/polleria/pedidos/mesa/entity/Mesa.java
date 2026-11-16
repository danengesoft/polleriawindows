package pe.edu.upeu.polleria.pedidos.mesa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MESAS", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String numero;
    @Column(nullable = false)
    private Integer capacidad;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoMesa estado = EstadoMesa.DISPONIBLE;
}
