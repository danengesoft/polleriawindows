package pe.edu.upeu.polleria.clinica.consultorio.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultorioResponse {
    private Long id;
    private Long clinicaId;
    private String clinicaRazonSocial;
    private String codigo;
    private BigDecimal tarifaHora;
    private String estado;
    private List<EspecialidadResponse> especialidades;
}
