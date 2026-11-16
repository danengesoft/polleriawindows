package pe.edu.upeu.polleria.pedidos.web.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WEB_SESIONES", schema = "POLLERIA")
@Getter
@Setter
@NoArgsConstructor
public class WebSesion {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "TOKEN_HASH", length = 64, nullable = false, unique = true)
    private String tokenHash;
    @Column(nullable = false)
    private LocalDateTime expira;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(nullable = false, precision = 1)
    private Boolean revocada = false;
}
