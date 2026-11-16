package pe.edu.upeu.polleria.pedidos.web.seguridad;

import java.util.*;
import org.springframework.stereotype.Component;

/**
 * Límite local acotado para el monolito actual; no confía en identidad enviada
 * en JSON.
 */
@Component
public class WebLimites {
    private record Ventana(long inicio, int cantidad) {
    }

    private final Map<String, Ventana> ventanas = new HashMap<>();

    public synchronized boolean admitir(String clave, int maximo) {
        long ahora = System.currentTimeMillis();
        ventanas.entrySet().removeIf(e -> ahora - e.getValue().inicio() > 60000);
        if (ventanas.size() >= 10000 && !ventanas.containsKey(clave))
            return false;
        var v = ventanas.get(clave);
        if (v == null) {
            ventanas.put(clave, new Ventana(ahora, 1));
            return true;
        }
        if (v.cantidad() >= maximo)
            return false;
        ventanas.put(clave, new Ventana(v.inicio(), v.cantidad() + 1));
        return true;
    }
}
