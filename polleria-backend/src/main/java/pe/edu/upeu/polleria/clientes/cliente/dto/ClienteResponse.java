package pe.edu.upeu.polleria.clientes.cliente.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private Boolean eliminable;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private String direccionPrincipal;
    private String referencia;
    private Boolean activo;
}
