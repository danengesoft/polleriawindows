package pe.edu.upeu.polleria.catalogo.producto.presentacion;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PRESENTACIONES_PRODUCTO", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class Presentacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "PRODUCTO_ID", nullable = false)
    private Long productoId;
    @Column(nullable = false, length = 60)
    private String nombre;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(nullable = false, precision = 1)
    private Boolean disponible = true;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(nullable = false, precision = 1)
    private Boolean eliminado = false;
}
