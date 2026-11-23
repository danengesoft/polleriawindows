package pe.edu.upeu.polleria.clinica.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.NumericBooleanConverter;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "CLINICAS", schema = "EXA_CLINICA")
@Getter
@Setter
@NoArgsConstructor
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // CHAR(11) en la base: el RUC siempre tiene 11 caracteres.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "RUC", nullable = false, length = 11, unique = true)
    private String ruc;

    @Column(name = "RAZON_SOCIAL", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    // La columna es SMALLINT 0/1; en Java se trabaja como Boolean.
    @Convert(converter = NumericBooleanConverter.class)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;
}
