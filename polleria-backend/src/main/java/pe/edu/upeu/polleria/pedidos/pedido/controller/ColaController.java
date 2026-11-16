package pe.edu.upeu.polleria.pedidos.pedido.controller;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.polleria.pedidos.pedido.service.PedidoServiceImpl;
import pe.edu.upeu.polleria.pedidos.pedido.dto.PedidoFiltro;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ColaController {
    private final PedidoServiceImpl service;

    public record Linea(String nombre, Integer cantidad) {
    }

    public record Ficha(Long id, TipoPedido tipoPedido, EstadoPedido estado, EstadoPago estadoPago,
            Long ocupacionMesaId, String mesaNumero, List<Linea> detalles) {
    }

    @GetMapping("/operaciones/pedidos")
    public List<Ficha> cocina() {
        return cola(EstadoPedido.PENDIENTE, false);
    }

    @GetMapping("/mesero/pedidos")
    public List<Ficha> mesero() {
        return cola(EstadoPedido.LISTO, true);
    }

    private List<Ficha> cola(EstadoPedido estado, boolean salon) {
        PedidoFiltro f = new PedidoFiltro();
        f.setEstado(estado);
        return service.buscar(f, "fecha", "ASC").stream().filter(p -> Integer.valueOf(2).equals(p.getFlujoVersion()))
                .filter(p -> Set.of(TipoPedido.MESA, TipoPedido.PARA_LLEVAR).contains(p.getTipoPedido()))
                .map(p -> new Ficha(p.getId(), p.getTipoPedido(), p.getEstado(), p.getEstadoPago(),
                        p.getOcupacionMesaId(), p.getMesaNumero(),
                        p.getDetalles().stream().map(d -> new Linea(d.getNombreProducto()
                                + (d.getNombrePresentacion() == null || d.getNombrePresentacion().equals("Única") ? ""
                                        : " · " + d.getNombrePresentacion()),
                                d.getCantidad())).toList()))
                .toList();
    }

    @GetMapping("/caja/me")
    public PedidoServiceImpl.CajaResumen caja() {
        return service.miCaja();
    }
}
