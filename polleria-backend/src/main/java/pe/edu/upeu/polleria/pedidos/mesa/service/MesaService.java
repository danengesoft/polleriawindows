package pe.edu.upeu.polleria.pedidos.mesa.service;

import java.util.List;
import pe.edu.upeu.polleria.pedidos.mesa.entity.EstadoMesa;

public interface MesaService {
    record Vista(Long id, String numero, Integer capacidad, EstadoMesa estado, Long ocupacionId) {
    }

    record Referencia(Long id, String numero) {
    }

    Referencia referencia(Long ocupacionId);

    List<Vista> listar();

    Long reservar(Long mesaId, Long clienteId);

    void cambiar(Long mesaId, Long ocupacionId, boolean liberar);
}
