package pe.edu.upeu.polleria.contenido.promocion.api;

import java.math.BigDecimal;

public interface PrecioPromocionService {
    default BigDecimal precioPresentacion(Long productoId, Long presentacionId, String promocionId,
            BigDecimal regular) {
        return precio(productoId, promocionId, regular);
    }

    BigDecimal precio(Long productoId, String promocionId, BigDecimal precioRegular);
}
