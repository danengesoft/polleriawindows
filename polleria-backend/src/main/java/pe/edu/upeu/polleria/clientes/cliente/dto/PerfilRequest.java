package pe.edu.upeu.polleria.clientes.cliente.dto;

import jakarta.validation.constraints.*;

public record PerfilRequest(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Size(max = 12) @Pattern(regexp = "^(\\+51)?[0-9]{7,9}$") String telefono,
        @Email @Size(max = 254) String correo,
        @Size(max = 300) String direccionPrincipal,
        @Size(max = 300) String referencia) {
    public ClienteRequest toInterno() {
        ClienteRequest r = new ClienteRequest();
        r.setNombres(nombres);
        r.setApellidos(apellidos);
        r.setTelefono(telefono);
        r.setCorreo(correo);
        r.setDireccionPrincipal(direccionPrincipal);
        r.setReferencia(referencia);
        return r;
    }
}
