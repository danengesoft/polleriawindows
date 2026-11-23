package pe.edu.upeu.polleria.clinica.especialidad.dto;

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
public class EspecialidadResponse {
    private Long id;
    private String codigo;
    private String nombre;
}
