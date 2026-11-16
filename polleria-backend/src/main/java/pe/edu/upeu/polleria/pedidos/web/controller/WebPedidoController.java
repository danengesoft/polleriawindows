package pe.edu.upeu.polleria.pedidos.web.controller;

import java.util.*;
import jakarta.servlet.http.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.polleria.pedidos.web.dto.*;
import pe.edu.upeu.polleria.pedidos.web.service.*;
import pe.edu.upeu.polleria.pedidos.mesa.service.MesaService;
import pe.edu.upeu.polleria.pedidos.eventos.EventosService;
import pe.edu.upeu.polleria.exception.InvalidRequestException;

@RestController
@RequestMapping("/api/v1/web")
@RequiredArgsConstructor

public class WebPedidoController {
    private final WebSesionService sesiones;
    private final WebPedidoService pedidos;
    private final MesaService mesas;
    private final EventosService eventos;
    private final ObjectMapper mapper;
    private final Validator validator;

    @PostMapping("/sesion")
    public Map<String, Object> sesion(HttpServletRequest req, HttpServletResponse res) {
        return sesiones.iniciar(req, res);
    }

    @PostMapping("/sesion/revocar")
    public void revocarSesion(HttpServletRequest r) {
        sesiones.revocar(WebSesionService.cookie(r));
    }

    @PostMapping("/pedidos")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public WebPedidoResponse crear(HttpServletRequest r,
            @RequestHeader(value = "Idempotency-Key", required = false) String clave, @RequestBody JsonNode body,
            Authentication auth) {
        campos(body, Set.of("comprador", "mesaId", "tipoPedido", "metodoPago", "direccionEntrega", "referenciaEntrega",
                "detalles"));
        campos(body.get("comprador"), Set.of("nombres", "apellidos", "telefono", "correo"));
        var detalles = body.get("detalles");
        if (detalles == null || !detalles.isArray())
            throw new InvalidRequestException("Revisa los productos del pedido");
        for (var d : detalles)
            campos(d, Set.of("productoId", "cantidad", "promocionId", "presentacionId"));
        WebPedidoRequest request;
        try {
            request = mapper.treeToValue(body, WebPedidoRequest.class);
        } catch (Exception e) {
            throw new InvalidRequestException("Formato de compra inválido");
        }
        if (!validator.validate(request).isEmpty())
            throw new InvalidRequestException("Revisa los datos del comprador y del pedido");
        String sub = auth instanceof JwtAuthenticationToken jwt
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))
                        ? jwt.getToken().getSubject()
                        : null;
        return pedidos.crear(WebSesionService.cookie(r), clave, sub, request);
    }

    @PostMapping("/pedidos/{id}/pago-prueba")
    public Map<String, Object> pagoPrueba(HttpServletRequest r, @PathVariable Long id, @RequestBody JsonNode body) {
        campos(body, Set.of("resultado"));
        var resultado = body.get("resultado");
        if (resultado == null || !resultado.isTextual())
            throw new InvalidRequestException("Resultado de prueba requerido");
        return pedidos.pagoPrueba(WebSesionService.cookie(r), id, resultado.asText());
    }

    @GetMapping("/pedidos/{id}")
    public WebPedidoResponse seguir(HttpServletRequest r, @PathVariable Long id) {
        return pedidos.seguir(WebSesionService.cookie(r), id);
    }

    @PostMapping("/pedidos/{id}/clave")
    public Map<String, String> clave(HttpServletRequest r, @PathVariable Long id) {
        return pedidos.claveRecuperacion(WebSesionService.cookie(r), id);
    }

    @PostMapping("/pedidos/{id}/recuperar")
    public WebPedidoResponse recuperar(HttpServletRequest r, @PathVariable Long id, @RequestBody JsonNode body) {
        campos(body, Set.of("clave"));
        if (body.get("clave") == null || !body.get("clave").isTextual())
            throw new InvalidRequestException("Clave de recuperación inválida");
        return pedidos.recuperar(WebSesionService.cookie(r), id, body.get("clave").asText());
    }

    @PostMapping("/pedidos/{id}/revocar")
    public void revocar(HttpServletRequest r, @PathVariable Long id) {
        pedidos.revocar(WebSesionService.cookie(r), id);
    }

    public record MesaPublica(Long id, String numero, Integer capacidad, Object estado) {
    }

    @GetMapping("/mesas")
    public List<MesaPublica> mesas() {
        return mesas.listar().stream().map(m -> new MesaPublica(m.id(), m.numero(), m.capacidad(), m.estado()))
                .toList();
    }

    @GetMapping(value = "/mesas/eventos", produces = "text/event-stream")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter eventos(HttpServletRequest r) {
        return eventos.conectarMesasPublicas(r.getRemoteAddr());
    }

    private static void campos(JsonNode n, Set<String> permitidos) {
        if (n == null || !n.isObject())
            throw new InvalidRequestException("Formato de compra inválido");
        for (String campo : n.propertyNames())
            if (!permitidos.contains(campo))
                throw new InvalidRequestException("La solicitud contiene campos no permitidos");
    }
}
