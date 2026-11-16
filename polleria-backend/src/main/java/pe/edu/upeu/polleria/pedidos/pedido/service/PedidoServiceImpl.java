package pe.edu.upeu.polleria.pedidos.pedido.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import org.springframework.data.domain.Sort;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.catalogo.producto.api.ProductoConsultaService;
import pe.edu.upeu.polleria.catalogo.producto.api.ProductoParaPedido;
import pe.edu.upeu.polleria.clientes.cliente.api.ClienteConsultaService;
import pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen;
import pe.edu.upeu.polleria.exception.*;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;
import pe.edu.upeu.polleria.pedidos.pedido.mapper.PedidoMapper;
import pe.edu.upeu.polleria.pedidos.pedido.repository.PedidoRepository;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {
    private final pe.edu.upeu.polleria.pedidos.web.repository.WebPedidoRepository pedidosWeb;
    private final pe.edu.upeu.polleria.contenido.promocion.api.PrecioPromocionService preciosPromocion;
    private final PedidoRepository pedidoRepository;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final ProductoConsultaService productoConsultaService;
    private final ClienteConsultaService clienteConsultaService;
    private final PedidoMapper pedidoMapper;
    private final pe.edu.upeu.polleria.pedidos.mesa.service.MesaService mesas;
    private final pe.edu.upeu.polleria.pedidos.eventos.EventosService eventos;

    private String actor() {
        var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return a == null || a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken ? null
                : a.getName();
    }

    private void exigirRol(String... roles) {
        var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getAuthorities().stream()
                .noneMatch(x -> java.util.Arrays.stream(roles).anyMatch(r -> x.getAuthority().equals("ROLE_" + r))))
            throw new org.springframework.security.access.AccessDeniedException("Operación no permitida para el rol");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return buscar(new PedidoFiltro(), "fecha", "DESC");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> buscar(PedidoFiltro filtro, String ordenarPor, String direccion) {
        validarFiltro(filtro);
        if (!List.of("fecha", "total").contains(ordenarPor)) {
            throw new InvalidRequestException("Orden permitido: fecha o total");
        }
        Sort.Direction sentido;
        try {
            sentido = Sort.Direction.valueOf(direccion.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Dirección de orden permitida: ASC o DESC");
        }
        Sort sort = Sort.by(sentido, ordenarPor).and(Sort.by(sentido, "id"));
        return pedidoRepository.buscar(filtro.getEstado(), filtro.getTipoPedido(), filtro.getClienteId(),
                desde(filtro), hasta(filtro), sort).stream().map(this::respuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResumenReporte resumen(PedidoFiltro filtro) {
        validarFiltro(filtro);
        PedidoAgregado agregado = pedidoRepository.agregado(filtro.getEstado(), filtro.getTipoPedido(),
                filtro.getClienteId(), desde(filtro), hasta(filtro));
        BigDecimal total = agregado.totalMonetario().setScale(2, RoundingMode.HALF_UP);
        BigDecimal promedio = agregado.cantidadPedidos() == 0 ? new BigDecimal("0.00")
                : total.divide(BigDecimal.valueOf(agregado.cantidadPedidos()), 2, RoundingMode.HALF_UP);
        return new PedidoResumenReporte(agregado.cantidadPedidos(), total, promedio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoVendidoReporte> productosMasVendidos(PedidoFiltro filtro) {
        return productosMasVendidos(filtro, 20);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoVendidoReporte> productosMasVendidos(PedidoFiltro filtro, int top) {
        validarFiltro(filtro);
        validarTop(top);
        return pedidoRepository.productosVendidos(filtro.getEstado(), filtro.getTipoPedido(),
                filtro.getClienteId(), desde(filtro), hasta(filtro),
                org.springframework.data.domain.PageRequest.of(0, top));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoPedidoReporte> porEstado(PedidoFiltro filtro) {
        validarFiltro(filtro);
        EnumMap<EstadoPedido, Long> cantidades = new EnumMap<>(EstadoPedido.class);
        pedidoRepository.porEstado(filtro.getEstado(), filtro.getTipoPedido(), filtro.getClienteId(),
                desde(filtro), hasta(filtro)).forEach(fila -> cantidades.put(fila.estado(), fila.cantidadPedidos()));
        return Arrays.stream(EstadoPedido.values())
                .map(estado -> new EstadoPedidoReporte(estado, cantidades.getOrDefault(estado, 0L))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoPedidoReporte> porTipo(PedidoFiltro filtro) {
        validarFiltro(filtro);
        EnumMap<TipoPedido, Long> cantidades = new EnumMap<>(TipoPedido.class);
        pedidoRepository.porTipo(filtro.getEstado(), filtro.getTipoPedido(), filtro.getClienteId(),
                desde(filtro), hasta(filtro))
                .forEach(fila -> cantidades.put(fila.tipoPedido(), fila.cantidadPedidos()));
        return Arrays.stream(TipoPedido.values())
                .map(tipo -> new TipoPedidoReporte(tipo, cantidades.getOrDefault(tipo, 0L))).toList();
    }

    private void validarFiltro(PedidoFiltro filtro) {
        if (filtro.getClienteId() != null && filtro.getClienteId() <= 0) {
            throw new InvalidRequestException("clienteId debe ser positivo");
        }
        if (filtro.getFechaDesde() != null && filtro.getFechaHasta() != null
                && filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {
            throw new InvalidRequestException("fechaDesde no puede ser posterior a fechaHasta");
        }
        // Oracle y el contrato ISO usan años de cuatro dígitos; además evita desbordar
        // plusDays.
        if ((filtro.getFechaDesde() != null
                && (filtro.getFechaDesde().getYear() < 1 || filtro.getFechaDesde().getYear() > 9999))
                || (filtro.getFechaHasta() != null
                        && (filtro.getFechaHasta().getYear() < 1 || filtro.getFechaHasta().getYear() >= 9999))) {
            throw new InvalidRequestException("Rango de fechas fuera del intervalo admitido");
        }
    }

    private LocalDateTime desde(PedidoFiltro filtro) {
        return filtro.getFechaDesde() == null ? null : filtro.getFechaDesde().atStartOfDay();
    }

    private LocalDateTime hasta(PedidoFiltro filtro) {
        return filtro.getFechaHasta() == null ? null : filtro.getFechaHasta().plusDays(1).atStartOfDay();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return respuesta(pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id)));
    }

    @Override
    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        return crearConActor(request, actor());
    }

    @Transactional
    public PedidoResponse crearWeb(PedidoRequest request, String sub) {
        return crearConActor(request, sub);
    }

    private PedidoResponse crearConActor(PedidoRequest request, String actorPedido) {
        if (!java.util.Set.of(TipoPedido.MESA, TipoPedido.PARA_LLEVAR).contains(request.getTipoPedido()))
            throw new InvalidRequestException("Tipo histórico no permitido en pedidos nuevos");
        if (request.getTipoPedido() == TipoPedido.MESA && request.getMesaId() == null)
            throw new InvalidRequestException("Selecciona una mesa disponible");
        if (request.getTipoPedido() != TipoPedido.MESA && request.getMesaId() != null)
            throw new InvalidRequestException("Solo los pedidos MESA admiten mesaId");
        ClienteResumen cliente = clienteConsultaService.consultarCliente(request.getClienteId());
        String direccion = opcional(request.getDireccionEntrega());

        Pedido pedido = new Pedido();
        pedido.setClienteId(cliente.id());
        pedido.setFlujoVersion(2);
        pedido.setCreadoPorSub(actorPedido);
        if (request.getTipoPedido() == TipoPedido.MESA)
            pedido.setOcupacionMesaId(mesas.reservar(request.getMesaId(), cliente.id()));
        pedido.setFecha(LocalDateTime.now(ZoneId.of("America/Lima")));
        pedido.setTipoPedido(request.getTipoPedido());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setEstadoPago(EstadoPago.PENDIENTE);
        pedido.setDireccionEntrega(direccion);
        pedido.setReferenciaEntrega(opcional(request.getReferenciaEntrega()));

        BigDecimal total = new BigDecimal("0.00");
        for (DetallePedidoRequest linea : request.getDetalles()) {
            ProductoParaPedido producto = productoConsultaService.consultarPresentacion(linea.getProductoId(),
                    linea.getPresentacionId());
            if (!Boolean.TRUE.equals(producto.disponible())) {
                throw new BusinessConflictException("Producto no disponible: " + producto.id());
            }
            if (producto.precio() == null || producto.precio().signum() <= 0) {
                throw new BusinessConflictException("Producto con precio inválido: " + producto.id());
            }
            BigDecimal precio = producto.precio().setScale(2, RoundingMode.UNNECESSARY);
            BigDecimal regular = precio;
            if (linea.getPromocionId() != null)
                precio = preciosPromocion.precioPresentacion(producto.id(), producto.presentacionId(),
                        linea.getPromocionId(), regular);
            BigDecimal subtotal = importe(precio.multiply(BigDecimal.valueOf(linea.getCantidad())));
            DetallePedido detalle = new DetallePedido();
            detalle.setProductoId(producto.id());
            detalle.setPresentacionId(producto.presentacionId());
            detalle.setNombrePresentacion(producto.nombrePresentacion());
            detalle.setNombreProducto(producto.nombre());
            detalle.setPrecioUnitario(precio);
            detalle.setPromocionId(linea.getPromocionId());
            detalle.setPrecioRegular(regular);
            detalle.setCantidad(linea.getCantidad());
            detalle.setSubtotal(subtotal);
            detalle.setPedido(pedido);
            pedido.getDetalles().add(detalle);
            total = importe(total.add(subtotal));
        }
        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.saveAndFlush(pedido);
        eventos.publicar("PEDIDO_CREADO", guardado.getId());
        return respuesta(guardado);
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedidoRequest request) {
        // Serializa cambios sobre la cabecera; no permite que dos solicitudes validen
        // el mismo estado previo.
        Pedido pedido = pedidoRepository.buscarParaCambioEstado(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));
        EstadoPedido origen = pedido.getEstado();
        EstadoPedido destino = request.getEstado();
        if (pedido.getFlujoVersion() != 2
                || !java.util.Set.of(TipoPedido.MESA, TipoPedido.PARA_LLEVAR).contains(pedido.getTipoPedido()))
            throw new BusinessConflictException("Pedido histórico de solo lectura operativa");
        boolean permitido = origen == EstadoPedido.PENDIENTE && destino == EstadoPedido.LISTO
                || origen == EstadoPedido.LISTO && destino == EstadoPedido.ENTREGADO;
        if (!permitido)
            throw new BusinessConflictException("Transición de pedido no permitida: " + origen + " → " + destino);
        if (destino == EstadoPedido.LISTO)
            exigirRol("OPERACIONES", "ADMINISTRADOR");
        else {
            exigirRol("MESERO", "ADMINISTRADOR");
            exigirPagado(pedido);
        }
        eventos.publicar(destino == EstadoPedido.LISTO ? "PEDIDO_LISTO" : "PEDIDO_ENTREGADO", id);
        pedido.setEstado(destino);
        return respuesta(pedidoRepository.saveAndFlush(pedido));
    }

    @Transactional
    public PedidoResponse crearMio(String sub, MiPedidoRequest request) {
        return crear(request.toInterno(clienteConsultaService.consultarPorSub(sub).id()));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMios(String sub) {
        PedidoFiltro filtro = new PedidoFiltro();
        filtro.setClienteId(clienteConsultaService.consultarPorSub(sub).id());
        return buscar(filtro, "fecha", "DESC");
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerMio(String sub, Long id) {
        Long clienteId = clienteConsultaService.consultarPorSub(sub).id();
        Pedido p = pedidoRepository.findById(id).filter(x -> clienteId.equals(x.getClienteId()))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        return respuesta(p);
    }

    @Transactional
    public PedidoResponse cancelarMio(String sub, Long id) {
        obtenerMio(sub, id);
        throw new BusinessConflictException("La cancelación no forma parte del nuevo flujo operativo");
    }

    @Override
    @Transactional
    public PedidoResponse confirmarPago(Long id) {
        Pedido p = pedidoRepository.buscarParaCambioEstado(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        exigirRol("ADMINISTRADOR", "CAJERO");
        if (!java.util.Set.of(TipoPedido.MESA, TipoPedido.PARA_LLEVAR).contains(p.getTipoPedido()))
            throw new BusinessConflictException("Pedido histórico de solo lectura operativa");
        if (p.getMetodoPago() != MetodoPago.EFECTIVO)
            throw new BusinessConflictException(
                    "Los pagos electrónicos requieren confirmación del proveedor; Caja solo confirma efectivo");
        if (p.getFlujoVersion() != 2 || p.getEstadoPago() != EstadoPago.PENDIENTE)
            throw new BusinessConflictException(
                    "Solo puede confirmarse un pago pendiente de un pedido nuevo");
        p.setEstadoPago(EstadoPago.PAGADO);
        p.setCobradoPorSub(actor());
        p.setFechaPago(LocalDateTime.now(ZoneId.of("America/Lima")));
        eventos.publicar("PEDIDO_PAGADO", id);
        return respuesta(pedidoRepository.saveAndFlush(p));
    }

    @org.springframework.beans.factory.annotation.Value("${polleria.pagos.simulador-habilitado:false}")
    private boolean simuladorHabilitado;

    /**
     * Exclusivamente el simulador local autorizado; no representa un cobro real.
     */
    @Transactional
    public void confirmarPagoSimulado(Long id) {
        if (!simuladorHabilitado)
            throw new ResourceNotFoundException("Simulador no disponible");
        Pedido p = pedidoRepository.buscarParaCambioEstado(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        // El canal web puede haber leído la entidad antes de obtener el bloqueo.
        // Refrescar evita reutilizar un estado PENDIENTE anterior a un cobro
        // concurrente.
        entityManager.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (p.getFlujoVersion() != 2 || p.getMetodoPago() == MetodoPago.EFECTIVO)
            throw new BusinessConflictException("Pago no compatible con el simulador electrónico");
        if (p.getEstadoPago() == EstadoPago.PAGADO)
            return;
        p.setEstadoPago(EstadoPago.PAGADO);
        p.setCobradoPorSub("SIMULADOR_LOCAL_SIN_COBRO_REAL");
        p.setFechaPago(LocalDateTime.now(ZoneId.of("America/Lima")));
        pedidoRepository.saveAndFlush(p);
        eventos.publicar("PEDIDO_PAGADO", id);
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> pagoPruebaInterno(Long id, String resultado) {
        exigirRol("ADMINISTRADOR", "CAJERO");
        if (!simuladorHabilitado)
            throw new ResourceNotFoundException("Simulador no disponible");
        if (!java.util.Set.of("CONSULTAR", "APROBAR", "RECHAZAR").contains(resultado))
            throw new InvalidRequestException("Resultado de prueba inválido");
        Pedido p = pedidoRepository.buscarParaCambioEstado(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (p.getFlujoVersion() != 2 || p.getMetodoPago() == MetodoPago.EFECTIVO)
            throw new BusinessConflictException(
                    "El efectivo se confirma en Caja y los pedidos históricos son de consulta");
        if (resultado.equals("APROBAR"))
            confirmarPagoSimulado(id);
        return java.util.Map.of("modo", "SIMULADOR_LOCAL", "pedidoId", id, "metodo", p.getMetodoPago(), "monto",
                p.getTotal(), "moneda", "PEN", "estadoPago", p.getEstadoPago(), "resultado",
                resultado.equals("RECHAZAR") && p.getEstadoPago() != EstadoPago.PAGADO ? "RECHAZADO"
                        : p.getEstadoPago().name(),
                "qrTexto", "POLLERIA_NEYOS_PRUEBA|PEDIDO=" + id + "|METODO=" + p.getMetodoPago() + "|PEN="
                        + p.getTotal().toPlainString());
    }

    private void exigirPagado(Pedido p) {
        if (p.getEstadoPago() != EstadoPago.PAGADO)
            throw new BusinessConflictException("Debes confirmar el pago antes de entregar");
    }

    public record CajaResumen(long cantidadCobros, BigDecimal totalCobrado) {
    }

    @Transactional(readOnly = true)
    public CajaResumen miCaja() {
        exigirRol("CAJERO", "ADMINISTRADOR");
        var inicio = java.time.LocalDate.now(ZoneId.of("America/Lima")).atStartOfDay();
        var lista = pedidoRepository.findByCobradoPorSubAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(actor(),
                inicio, inicio.plusDays(1));
        return new CajaResumen(lista.size(),
                lista.stream().map(Pedido::getTotal).reduce(new BigDecimal("0.00"), BigDecimal::add));
    }

    private PedidoResponse respuesta(Pedido pedido) {
        var r = pedidoMapper.toResponse(pedido, clienteConsultaService.consultarCliente(pedido.getClienteId()));
        if (pedido.getOcupacionMesaId() != null) {
            var mesa = mesas.referencia(pedido.getOcupacionMesaId());
            r.setMesaId(mesa.id());
            r.setMesaNumero(mesa.numero());
        }
        return r;
    }

    private BigDecimal importe(BigDecimal valor) {
        BigDecimal resultado = valor.setScale(2, RoundingMode.UNNECESSARY);
        if (resultado.precision() > 19) {
            throw new InvalidRequestException("El importe supera la capacidad monetaria del pedido");
        }
        return resultado;
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaDiaReporte> ventasPorDia(PedidoFiltro filtro) {
        validarFiltro(filtro);
        return pedidoRepository.ventasPorDia(filtro.getEstado(), filtro.getTipoPedido(), filtro.getClienteId(),
                desde(filtro), hasta(filtro));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoReporte> metodosPago(PedidoFiltro filtro) {
        validarFiltro(filtro);
        EnumMap<MetodoPago, MetodoPagoReporte> filas = new EnumMap<>(MetodoPago.class);
        pedidoRepository.metodosPago(filtro.getEstado(), filtro.getTipoPedido(), filtro.getClienteId(),
                desde(filtro), hasta(filtro)).forEach(fila -> filas.put(fila.metodoPago(), fila));
        return Arrays.stream(MetodoPago.values()).map(metodo -> filas.getOrDefault(metodo,
                new MetodoPagoReporte(metodo, 0L, new BigDecimal("0.00")))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteCompradorReporte> clientesCompradores(PedidoFiltro filtro, int top) {
        validarFiltro(filtro);
        validarTop(top);
        return pedidoRepository.clientesCompradores(filtro.getEstado(), filtro.getTipoPedido(), filtro.getClienteId(),
                desde(filtro), hasta(filtro), org.springframework.data.domain.PageRequest.of(0, top));
    }

    private void validarTop(int top) {
        if (top < 1 || top > 100)
            throw new InvalidRequestException("top debe estar entre 1 y 100");
    }

}
