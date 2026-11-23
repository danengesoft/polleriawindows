package pe.edu.upeu.polleria.clinica.consultorio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.polleria.clinica.consultorio.entity.EstadoConsultorio;

/** Cabecera (datos del consultorio) más sus líneas: los id de las especialidades. */
@Getter
@Setter
public class ConsultorioRequest {

    @NotNull(message = "Elige la clínica")
    @Positive
    private Long clinicaId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10, message = "El código admite hasta 10 caracteres")
    private String codigo;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0", inclusive = false, message = "La tarifa debe ser mayor que 0")
    @Digits(integer = 6, fraction = 2, message = "La tarifa admite hasta 6 enteros y 2 decimales")
    private BigDecimal tarifaHora;

    @NotNull(message = "Elige el estado")
    private EstadoConsultorio estado;

    @NotEmpty(message = "Agrega al menos una especialidad")
    private List<@NotNull @Positive Long> especialidadIds;
}
