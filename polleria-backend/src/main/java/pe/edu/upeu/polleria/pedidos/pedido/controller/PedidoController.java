package pe.edu.upeu.polleria.pedidos.pedido.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;
import pe.edu.upeu.polleria.pedidos.pedido.service.PedidoService;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService service;

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> buscar(
            @Valid @ModelAttribute PedidoFiltro filtro,
            @RequestParam(defaultValue = "fecha") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion) {
        return ResponseEntity.ok(service.buscar(filtro, ordenarPor, direccion));
    }

    @GetMapping("/reportes/resumen")
    public ResponseEntity<PedidoResumenReporte> resumen(@Valid @ModelAttribute PedidoFiltro filtro) {
        return ResponseEntity.ok(service.resumen(filtro));
    }

    @GetMapping("/reportes/productos-mas-vendidos")
    public ResponseEntity<List<ProductoVendidoReporte>> productosMasVendidos(
            @Valid @ModelAttribute PedidoFiltro filtro,
            @RequestParam(defaultValue = "20") int top) {
        return ResponseEntity.ok(service.productosMasVendidos(filtro, top));
    }

    @GetMapping("/reportes/por-estado")
    public ResponseEntity<List<EstadoPedidoReporte>> porEstado(
            @Valid @ModelAttribute PedidoFiltro filtro) {
        return ResponseEntity.ok(service.porEstado(filtro));
    }

    @GetMapping("/reportes/por-tipo")
    public ResponseEntity<List<TipoPedidoReporte>> porTipo(
            @Valid @ModelAttribute PedidoFiltro filtro) {
        return ResponseEntity.ok(service.porTipo(filtro));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + response.getId())).body(response);
    }

    @PatchMapping("/{id}/confirmar-pago")
    public ResponseEntity<PedidoResponse> confirmarPago(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmarPago(id));
    }

    @PostMapping("/{id}/pago-prueba")
    public java.util.Map<String,Object> pagoPrueba(@PathVariable Long id,@RequestBody java.util.Map<String,String> body){
        if(!body.keySet().equals(java.util.Set.of("resultado"))||body.get("resultado")==null)throw new pe.edu.upeu.polleria.exception.InvalidRequestException("Resultado de prueba requerido");
        return service.pagoPruebaInterno(id,body.get("resultado"));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponse> cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody EstadoPedidoRequest request) {
        return ResponseEntity.ok(service.cambiarEstado(id, request));
    }

    @GetMapping("/reportes/ventas-por-dia")
    public List<VentaDiaReporte> ventasPorDia(@Valid @ModelAttribute PedidoFiltro filtro) {
        return service.ventasPorDia(filtro);
    }

    @GetMapping("/reportes/metodos-pago")
    public List<MetodoPagoReporte> metodosPago(@Valid @ModelAttribute PedidoFiltro filtro) {
        return service.metodosPago(filtro);
    }

    @GetMapping("/reportes/clientes")
    public List<ClienteCompradorReporte> clientesCompradores(
            @Valid @ModelAttribute PedidoFiltro filtro,
            @RequestParam(defaultValue = "20") int top) {
        return service.clientesCompradores(filtro, top);
    }

}
