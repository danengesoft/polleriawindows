package pe.edu.upeu.polleria.pedidos.web.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.polleria.pedidos.web.dto.*;
import pe.edu.upeu.polleria.pedidos.web.entity.*;
import pe.edu.upeu.polleria.pedidos.web.repository.*;
import pe.edu.upeu.polleria.pedidos.web.seguridad.WebCripto;
import pe.edu.upeu.polleria.pedidos.pedido.service.PedidoServiceImpl;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;
import pe.edu.upeu.polleria.clientes.cliente.api.ClienteConsultaService;
import pe.edu.upeu.polleria.exception.*;

@Service
@RequiredArgsConstructor
public class WebPedidoService {
    @org.springframework.beans.factory.annotation.Value("${polleria.pagos.simulador-habilitado:false}")
    private boolean simulador;
    private final WebSesionService sesiones;
    private final WebPedidoRepository web;
    private final PedidoServiceImpl pedidos;
    private final ClienteConsultaService clientes;

    @Transactional
    public WebPedidoResponse crear(String token, String clave, String sub, WebPedidoRequest r) {
        if (clave == null
                || !clave.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"))
            throw new InvalidRequestException("Falta una clave válida de confirmación");
        var sesion = sesiones.bloquear(token);
        var d = r.comprador();
        String huella = WebCripto.huella(sub, d.nombres(), d.apellidos(), d.telefono(), d.correo(), r.tipoPedido(),
                r.metodoPago(), r.mesaId(), r.direccionEntrega(), r.referenciaEntrega(), r.detalles());
        var anterior = web.findByClaveIdempotencia(clave);
        if (anterior.isPresent()) {
            var a = anterior.get();
            if (!sesion.getId().equals(a.getSesionId()))
                throw WebSesionService.inaccesible();
            if (!WebCripto.igual(a.getHuellaSolicitud(), huella))
                throw new BusinessConflictException(
                        "La confirmación ya fue utilizada con otros datos; revisa el pedido anterior");
            return autorizado(sesion, a);
        }
        Set<String> productos = new HashSet<>();
        for (var linea : r.detalles())
            if (!productos.add(linea.productoId() + ":" + linea.presentacionId() + ":" + linea.promocionId()))
                throw new InvalidRequestException("No repitas productos en el pedido");
        var comprador = clientes.resolverCompradorWeb(sesion.getId(), sub, d,
                WebCripto.huella(d.nombres(), d.apellidos(), d.telefono(), d.correo()));
        PedidoRequest interno = new PedidoRequest();
        interno.setClienteId(comprador.id());
        interno.setMesaId(r.mesaId());
        interno.setTipoPedido(r.tipoPedido());
        interno.setMetodoPago(r.metodoPago());
        interno.setDireccionEntrega(r.direccionEntrega());
        interno.setReferenciaEntrega(r.referenciaEntrega());
        interno.setDetalles(r.detalles().stream().map(l -> {
            var x = new DetallePedidoRequest();
            x.setProductoId(l.productoId());
            x.setCantidad(l.cantidad());
            x.setPromocionId(l.promocionId());
            x.setPresentacionId(l.presentacionId());
            return x;
        }).toList());
        var resultado = pedidos.crearWeb(interno, sub);
        WebPedido acceso = new WebPedido();
        acceso.setPedidoId(resultado.getId());
        acceso.setSesionId(sesion.getId());
        acceso.setSesionAccesoId(sesion.getId());
        acceso.setClaveIdempotencia(clave);
        acceso.setHuellaSolicitud(huella);
        acceso.setUsuarioSub(sub);
        acceso.setNombres(d.nombres());
        acceso.setApellidos(d.apellidos());
        acceso.setTelefono(d.telefono());
        acceso.setCorreoContacto(d.correo());
        web.saveAndFlush(acceso);
        return WebPedidoResponse.de(resultado);
    }

    @Transactional(readOnly = true)
    public WebPedidoResponse seguir(String token, Long id) {
        var s = sesiones.validar(token);
        return autorizado(s, web.findById(id).orElseThrow(WebSesionService::inaccesible));
    }

    private WebPedidoResponse autorizado(WebSesion s, WebPedido p) {
        if (!s.getId().equals(p.getSesionAccesoId()))
            throw WebSesionService.inaccesible();
        return WebPedidoResponse.de(pedidos.obtener(p.getPedidoId()));
    }

    @Transactional
    public Map<String, String> claveRecuperacion(String token, Long id) {
        var s = sesiones.bloquear(token);
        var p = web.bloquear(id).orElseThrow(WebSesionService::inaccesible);
        autorizado(s, p);
        String clave = WebCripto.secreto();
        p.setRecuperacionHash(WebCripto.hash(clave));
        p.setRecuperacionExpira(WebSesionService.ahora().plusDays(30));
        web.saveAndFlush(p);
        return Map.of("clave", clave, "expira", p.getRecuperacionExpira().toString() + "Z");
    }

    @Transactional
    public WebPedidoResponse recuperar(String token, Long id, String clave) {
        var s = sesiones.bloquear(token);
        var p = web.bloquear(id).orElseThrow(WebSesionService::inaccesible);
        if (!WebCripto.valido(clave) || p.getRecuperacionExpira() == null
                || !p.getRecuperacionExpira().isAfter(WebSesionService.ahora())
                || !WebCripto.igual(p.getRecuperacionHash(), WebCripto.hash(clave)))
            throw WebSesionService.inaccesible();
        p.setSesionAccesoId(s.getId());
        p.setRecuperacionHash(null);
        p.setRecuperacionExpira(null);
        web.saveAndFlush(p);
        return autorizado(s, p);
    }

    @Transactional
    public Map<String, Object> pagoPrueba(String token, Long id, String resultado) {
        if (!simulador)
            throw WebSesionService.inaccesible();
        var sesion = sesiones.bloquear(token);
        var acceso = web.bloquear(id).orElseThrow(WebSesionService::inaccesible);
        var p = autorizado(sesion, acceso);
        if (p.metodoPago() == pe.edu.upeu.polleria.pedidos.pedido.entity.MetodoPago.EFECTIVO)
            throw new BusinessConflictException("El efectivo se confirma exclusivamente en Caja");
        if (!Set.of("CONSULTAR", "APROBAR", "RECHAZAR").contains(resultado))
            throw new InvalidRequestException("Resultado de prueba inválido");
        if (resultado.equals("APROBAR")) {
            pedidos.confirmarPagoSimulado(id);
            p = autorizado(sesion, acceso);
        }
        return Map.of("modo", "SIMULADOR_LOCAL", "pedidoId", id, "metodo", p.metodoPago(), "monto", p.total(), "moneda",
                "PEN", "estadoPago", p.estadoPago(), "resultado",
                resultado.equals("RECHAZAR")
                        && p.estadoPago() != pe.edu.upeu.polleria.pedidos.pedido.entity.EstadoPago.PAGADO ? "RECHAZADO"
                                : p.estadoPago().name(),
                "qrTexto", "POLLERIA_NEYOS_PRUEBA|PEDIDO=" + id + "|METODO=" + p.metodoPago() + "|PEN="
                        + p.total().toPlainString());
    }

    @Transactional
    public void revocar(String token, Long id) {
        var s = sesiones.bloquear(token);
        var p = web.bloquear(id).orElseThrow(WebSesionService::inaccesible);
        autorizado(s, p);
        p.setSesionAccesoId(null);
        p.setRecuperacionHash(null);
        p.setRecuperacionExpira(null);
        web.saveAndFlush(p);
    }
}
