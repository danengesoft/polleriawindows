package pe.edu.upeu.polleria.clinica.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicaRequest {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social admite hasta 150 caracteres")
    private String razonSocial;

    @Size(max = 20, message = "El teléfono admite hasta 20 caracteres")
    private String telefono;

    @NotNull
    private Boolean activo;
}
