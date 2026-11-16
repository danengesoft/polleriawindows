package pe.edu.upeu.polleria.pedidos.web.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WEB_PEDIDOS", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class WebPedido {
    @Id
    @Column(name = "PEDIDO_ID")
    private Long pedidoId;
    @Column(name = "SESION_ID", length = 36, nullable = false)
    private String sesionId;
    @Column(name = "SESION_ACCESO_ID", length = 36)
    private String sesionAccesoId;
    @Column(name = "CLAVE_IDEMPOTENCIA", length = 36, nullable = false)
    private String claveIdempotencia;
    @Column(name = "HUELLA_SOLICITUD", length = 64, nullable = false)
    private String huellaSolicitud;
    @Column(name = "USUARIO_SUB", length = 255)
    private String usuarioSub;
    @Column(length = 100, nullable = false)
    private String nombres;
    @Column(length = 100, nullable = false)
    private String apellidos;
    @Column(length = 12, nullable = false)
    private String telefono;
    @Column(name = "CORREO_CONTACTO", length = 254)
    private String correoContacto;
    @Column(name = "RECUPERACION_HASH", length = 64)
    private String recuperacionHash;
    @Column(name = "RECUPERACION_EXPIRA")
    private LocalDateTime recuperacionExpira;
}
