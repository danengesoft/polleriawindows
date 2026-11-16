package pe.edu.upeu.polleria.catalogo.producto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCTOS", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class Producto {
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(name = "ELIMINADO", nullable = false, precision = 1)
    private Boolean eliminado = false;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 120)
    private String nombre;
    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;
    @Column(name = "PRECIO", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal precio;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(name = "DISPONIBLE", nullable = false, precision = 1)
    private Boolean disponible;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private pe.edu.upeu.polleria.catalogo.categoria.entity.Categoria categoria;
    @Column(name = "IMAGEN_URL", length = 1000)
    private String imagenUrl;
}
