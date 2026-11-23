package pe.edu.upeu.polleria.clinica.consultorio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

/**
 * Cabecera: el consultorio. Sus líneas (detalles) son las especialidades,
 * guardadas en la tabla intermedia CONSULTORIO_ESPECIALIDAD (muchos a muchos).
 */
@Entity
@Table(name = "CONSULTORIOS", schema = "EXA_CLINICA")
@Getter
@Setter
@NoArgsConstructor
public class Consultorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLINICA", nullable = false)
    private Clinica clinica;

    @Column(name = "CODIGO", nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(name = "TARIFA_HORA", nullable = false, precision = 8, scale = 2)
    private BigDecimal tarifaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoConsultorio estado = EstadoConsultorio.DISPONIBLE;

    // El consultorio es el dueño de la relación: al guardarlo se insertan las filas de
    // CONSULTORIO_ESPECIALIDAD y al eliminarlo se borran antes que la cabecera.
    @ManyToMany
    @OrderBy("codigo ASC")
    @JoinTable(name = "CONSULTORIO_ESPECIALIDAD", schema = "EXA_CLINICA",
            joinColumns = @JoinColumn(name = "ID_CONSULTORIO"),
            inverseJoinColumns = @JoinColumn(name = "ID_ESPECIALIDAD"))
    private Set<Especialidad> especialidades = new LinkedHashSet<>();
}
