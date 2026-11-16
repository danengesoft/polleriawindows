package pe.edu.upeu.polleria.pedidos.web.service;

import java.time.*;
import java.util.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.pedidos.web.entity.WebSesion;
import pe.edu.upeu.polleria.pedidos.web.repository.WebSesionRepository;
import pe.edu.upeu.polleria.pedidos.web.seguridad.WebCripto;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class WebSesionService {
    public static final String COOKIE = "LB_WEB";
    private final WebSesionRepository repo;
    @Value("${polleria.web.cookie-secure:true}")
    private boolean segura;

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static String cookie(HttpServletRequest r) {
        if (r.getCookies() != null)
            for (Cookie c : r.getCookies())
                if (COOKIE.equals(c.getName()))
                    return c.getValue();
        return null;
    }

    @Transactional
    public Map<String, Object> iniciar(HttpServletRequest req, HttpServletResponse res) {
        String token = cookie(req);
        WebSesion s = WebCripto.valido(token)
                ? repo.findByTokenHash(WebCripto.hash(token)).filter(this::vigente).orElse(null)
                : null;
        if (s == null) {
            token = WebCripto.secreto();
            s = new WebSesion();
            s.setId(UUID.randomUUID().toString());
            s.setTokenHash(WebCripto.hash(token));
            s.setExpira(ahora().plusDays(7));
            repo.saveAndFlush(s);
        }
        res.addHeader("Set-Cookie",
                ResponseCookie.from(COOKIE, token).httpOnly(true).secure(segura).sameSite("Lax").path("/api/v1/web")
                        .maxAge(Math.max(0, Duration.between(ahora(), s.getExpira()).getSeconds())).build().toString());
        return Map.of("csrf", WebCripto.csrf(token), "expira", s.getExpira().toString() + "Z");
    }

    public boolean vigente(WebSesion s) {
        return !Boolean.TRUE.equals(s.getRevocada()) && s.getExpira().isAfter(ahora());
    }

    @Transactional(readOnly = true)
    public WebSesion validar(String token) {
        return buscar(token, false);
    }

    @Transactional
    public WebSesion bloquear(String token) {
        return buscar(token, true);
    }

    private WebSesion buscar(String token, boolean bloqueo) {
        if (!WebCripto.valido(token))
            throw inaccesible();
        return (bloqueo ? repo.bloquear(WebCripto.hash(token)) : repo.findByTokenHash(WebCripto.hash(token)))
                .filter(this::vigente).orElseThrow(WebSesionService::inaccesible);
    }

    @Transactional
    public void revocar(String token) {
        var s = bloquear(token);
        s.setRevocada(true);
        repo.saveAndFlush(s);
    }

    public static ResourceNotFoundException inaccesible() {
        return new ResourceNotFoundException("No se pudo acceder al pedido con esta credencial");
    }
}
