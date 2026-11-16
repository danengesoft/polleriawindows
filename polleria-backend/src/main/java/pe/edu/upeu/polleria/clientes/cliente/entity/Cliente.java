package pe.edu.upeu.polleria.clientes.cliente.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CLIENTES", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NOMBRES", nullable = false, length = 100)
    private String nombres;
    @Column(name = "APELLIDOS", nullable = false, length = 100)
    private String apellidos;
    @Column(name = "TELEFONO", nullable = false, length = 12)
    private String telefono;
    @Column(name = "CORREO", length = 254)
    private String correo;
    @Column(name = "DIRECCION_PRINCIPAL", length = 300)
    private String direccionPrincipal;
    @Column(name = "REFERENCIA", length = 300)
    private String referencia;
    @Column(name = "KEYCLOAK_SUB", length = 255, unique = true)
    private String keycloakSub;
    @Column(name = "WEB_SESION_ID", length = 36)
    private String webSesionId;
    @Column(name = "WEB_HUELLA", length = 64)
    private String webHuella;
    @Column(name = "CORREO_CONTACTO_WEB", length = 254)
    private String correoContactoWeb;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(name = "ACTIVO", nullable = false, precision = 1)
    private Boolean activo = true;
}
