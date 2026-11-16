package pe.edu.upeu.polleria.clientes.cliente.api;

import jakarta.validation.constraints.*;

public record DatosCompradorWeb(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "^(\\+51)?[0-9]{7,9}$") @Size(max = 12) String telefono,
        @Email @Size(max = 254) String correo) {
    public DatosCompradorWeb {
        nombres = normalizar(nombres);
        apellidos = normalizar(apellidos);
        telefono = telefono == null ? null : telefono.strip();
        correo = correo == null || correo.isBlank() ? null : correo.strip().toLowerCase(java.util.Locale.ROOT);
    }

    private static String normalizar(String v) {
        return v == null ? null : v.strip().replaceAll("\\s+", " ");
    }
}
