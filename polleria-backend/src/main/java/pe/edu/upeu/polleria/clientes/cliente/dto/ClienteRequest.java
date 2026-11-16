package pe.edu.upeu.polleria.clientes.cliente.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequest {
    @NotBlank
    @Size(max = 100)
    private String nombres;
    @NotBlank
    @Size(max = 100)
    private String apellidos;
    @NotBlank
    @Size(max = 12)
    @Pattern(regexp = "^(\\+51)?[0-9]{7,9}$")
    private String telefono;
    @Email
    @Size(max = 254)
    private String correo;
    @Size(max = 300)
    private String direccionPrincipal;
    @Size(max = 300)
    private String referencia;
    @NotNull
    private Boolean activo = true;

    // Retira espacios exteriores antes de @Email; la normalización comercial vive
    // en ServiceImpl.
    public void setCorreo(String correo) {
        this.correo = correo == null ? null : correo.strip();
    }
}
