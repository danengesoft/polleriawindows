package pe.edu.upeu.polleria.clinica.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicaResponse {
    private Long id;
    private String ruc;
    private String razonSocial;
    private String telefono;
    private Boolean activo;
}
