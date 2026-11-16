package pe.edu.upeu.polleria.pedidos.mesa.service;

import java.util.*;
import java.time.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.upeu.polleria.pedidos.mesa.entity.*;
import pe.edu.upeu.polleria.pedidos.mesa.repository.*;
import pe.edu.upeu.polleria.pedidos.eventos.EventosService;
import pe.edu.upeu.polleria.exception.*;

@Service
@RequiredArgsConstructor
public class MesaServiceImpl implements MesaService {
    private final MesaRepository mesas;
    private final OcupacionMesaRepository ocupaciones;
    private final EventosService eventos;

    @Transactional(readOnly = true)
    public List<Vista> listar() {
        return mesas.findAll().stream()
                .map(m -> new Vista(m.getId(), m.getNumero(), m.getCapacidad(), m.getEstado(), ocupaciones
                        .findByMesaIdAndFechaLiberacionIsNull(m.getId()).map(OcupacionMesa::getId).orElse(null)))
                .toList();
    }

    @Transactional
    public Long reservar(Long mesaId, Long clienteId) {
        Mesa m = buscar(mesaId);
        if (m.getEstado() != EstadoMesa.DISPONIBLE)
            throw new BusinessConflictException("La mesa ya no está disponible");
        OcupacionMesa o = new OcupacionMesa();
        o.setMesa(m);
        o.setClienteId(clienteId);
        o.setFechaReserva(LocalDateTime.now(ZoneId.of("America/Lima")));
        ocupaciones.saveAndFlush(o);
        m.setEstado(EstadoMesa.RESERVADA);
        mesas.saveAndFlush(m);
        eventos.publicar("MESA_RESERVADA", m.getId());
        return o.getId();
    }

    @Transactional
    public void cambiar(Long mesaId, Long ocupacionId, boolean liberar) {
        Mesa m = buscar(mesaId);
        OcupacionMesa o = ocupaciones.findByMesaIdAndFechaLiberacionIsNull(mesaId)
                .filter(x -> x.getId().equals(ocupacionId))
                .orElseThrow(() -> new BusinessConflictException("La ocupación ya no está vigente"));
        if (liberar) {
            if (m.getEstado() != EstadoMesa.OCUPADA)
                throw new BusinessConflictException("Confirma la llegada antes de liberar la mesa");
            o.setFechaLiberacion(LocalDateTime.now(ZoneId.of("America/Lima")));
            o.setLiberadaPorSub(SecurityContextHolder.getContext().getAuthentication().getName());
            m.setEstado(EstadoMesa.DISPONIBLE);
        } else {
            if (m.getEstado() != EstadoMesa.RESERVADA)
                throw new BusinessConflictException("La mesa debe estar reservada");
            m.setEstado(EstadoMesa.OCUPADA);
            o.setFechaLlegada(LocalDateTime.now(ZoneId.of("America/Lima")));
        }
        ocupaciones.saveAndFlush(o);
        mesas.saveAndFlush(m);
        eventos.publicar(liberar ? "MESA_LIBERADA" : "MESA_OCUPADA", mesaId);
    }

    @Transactional(readOnly = true)
    public Referencia referencia(Long id) {
        var o = ocupaciones.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ocupación no encontrada"));
        return new Referencia(o.getMesa().getId(), o.getMesa().getNumero());
    }

    private Mesa buscar(Long id) {
        return mesas.bloquear(id).orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));
    }
}
