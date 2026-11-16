package pe.edu.upeu.polleria.catalogo.categoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CATEGORIAS", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(name="ELIMINADO",nullable=false,precision=1)
    private Boolean eliminado=false;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NOMBRE", nullable = false, length = 80)
    private String nombre;
    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(name = "ACTIVO", nullable = false, precision = 1)
    private Boolean activo;
}
