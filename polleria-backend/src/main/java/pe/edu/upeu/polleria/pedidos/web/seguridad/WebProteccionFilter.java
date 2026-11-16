package pe.edu.upeu.polleria.pedidos.web.seguridad;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.polleria.pedidos.web.service.WebSesionService;

@Component
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 20)
public class WebProteccionFilter extends OncePerRequestFilter {
    private final Set<String> origenes;
    private final WebLimites limites;

    public WebProteccionFilter(@Value("${polleria.cors.allowed-origins:}") String origenes, WebLimites limites) {
        this.origenes = new HashSet<>(Arrays.asList(origenes.split(",")));
        this.limites = limites;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String ruta = req.getRequestURI();
        if (!ruta.startsWith("/api/v1/web/") || req.getMethod().equals("OPTIONS")) {
            chain.doFilter(req, res);
            return;
        }
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Referrer-Policy", "no-referrer");
        res.setHeader("X-Content-Type-Options", "nosniff");
        String ip = req.getRemoteAddr();
        int maximo = req.getMethod().equals("GET") ? 180 : 30;
        if (!limites.admitir(ip + ":" + req.getMethod() + ":" + (ruta.endsWith("recuperar") ? "recuperar" : "web"),
                maximo)) {
            error(res, 429, "Demasiadas solicitudes; espera un momento");
            return;
        }
        if (!Set.of("GET", "HEAD").contains(req.getMethod())) {
            if (Collections.list(req.getHeaders("Origin")).size() != 1 || !origenes.contains(req.getHeader("Origin"))) {
                error(res, 403, "Origen de solicitud no permitido");
                return;
            }
            if (!ruta.equals("/api/v1/web/sesion")) {
                String token = WebSesionService.cookie(req);
                if (!WebCripto.valido(token) || !WebCripto.igual(WebCripto.csrf(token), req.getHeader("X-Web-CSRF"))) {
                    error(res, 403, "La sesión de compra debe actualizarse antes de continuar");
                    return;
                }
            }
            byte[] body = req.getInputStream().readNBytes(65537);
            if (body.length > 65536) {
                error(res, 413, "Solicitud demasiado grande");
                return;
            }
            var original = req;
            req = new HttpServletRequestWrapper(original) {
                @Override
                public ServletInputStream getInputStream() {
                    var in = new ByteArrayInputStream(body);
                    return new ServletInputStream() {
                        public int read() {
                            return in.read();
                        }

                        public boolean isFinished() {
                            return in.available() == 0;
                        }

                        public boolean isReady() {
                            return true;
                        }

                        public void setReadListener(ReadListener l) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public BufferedReader getReader() {
                    return new BufferedReader(
                            new InputStreamReader(getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                }
            };
        }
        chain.doFilter(req, res);
    }

    private static void error(HttpServletResponse r, int status, String mensaje) throws IOException {
        r.setStatus(status);
        r.setContentType("application/json");
        r.setCharacterEncoding("UTF-8");
        if (status == 429)
            r.setHeader("Retry-After", "60");
        r.getWriter().write("{\"status\":" + status + ",\"message\":\"" + mensaje + "\"}");
    }
}
