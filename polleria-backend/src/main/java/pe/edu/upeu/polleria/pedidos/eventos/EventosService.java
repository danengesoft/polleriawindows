package pe.edu.upeu.polleria.pedidos.eventos;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.event.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.annotation.PreDestroy;

@Service
public class EventosService {
  private record Conexion(SseEmitter emitter, Set<String> roles, String ip) {
  }

  private final List<Conexion> conexiones = new CopyOnWriteArrayList<>();
  private final ApplicationEventPublisher publisher;
  private final AtomicLong secuencia = new AtomicLong();
  private final ScheduledExecutorService reloj = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread t = new Thread(r, "la-brava-sse");
    t.setDaemon(true);
    return t;
  });

  public EventosService(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
    reloj.scheduleAtFixedRate(this::latido, 15, 15, TimeUnit.SECONDS);
  }

  public void publicar(String tipo, Long id) {
    publisher.publishEvent(new CambioOperativo(tipo, id));
  }

  public static boolean permitido(String tipo, Set<String> roles) {
    if (roles.contains("ADMINISTRADOR"))
      return true;
    if (tipo.startsWith("MESA_") && roles.contains("MESAS_PUBLICAS"))
      return true;
    if (tipo.startsWith("MESA_"))
      return !Collections.disjoint(roles, Set.of("CLIENTE", "MESERO", "CAJERO"));
    if (tipo.equals("PEDIDO_PAGADO"))
      return !Collections.disjoint(roles, Set.of("CAJERO", "MESERO"));
    return !Collections.disjoint(roles, Set.of("CAJERO", "MESERO", "OPERACIONES"));
  }

  public SseEmitter conectar() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    Set<String> roles = new HashSet<>();
    auth.getAuthorities().forEach(a -> roles.add(a.getAuthority().replace("ROLE_", "")));
    SseEmitter emitter = new SseEmitter(0L);
    Conexion c = new Conexion(emitter, roles, null);
    conexiones.add(c);
    if (auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt && jwt.getExpiresAt() != null) {
      long segundos = java.time.Duration.between(java.time.Instant.now(), jwt.getExpiresAt()).getSeconds();
      reloj.schedule(emitter::complete, Math.max(0, segundos), TimeUnit.SECONDS);
    }
    emitter.onCompletion(() -> conexiones.remove(c));
    emitter.onTimeout(() -> conexiones.remove(c));
    emitter.onError(e -> conexiones.remove(c));
    try {
      emitter.send(SseEmitter.event().name("RECUPERAR").data(Map.of("accion", "REST")));
    } catch (Exception e) {
      conexiones.remove(c);
      emitter.complete();
    }
    return emitter;
  }

  public synchronized SseEmitter conectarMesasPublicas(String ip) {
    if (conexiones.stream().filter(c -> c.ip() != null).count() >= 200
        || conexiones.stream().filter(c -> ip.equals(c.ip())).count() >= 20)
      throw new pe.edu.upeu.polleria.exception.BusinessConflictException(
          "Límite de conexiones de mapa; reintenta más tarde");
    SseEmitter emitter = new SseEmitter(120000L);
    Conexion c = new Conexion(emitter, Set.of("MESAS_PUBLICAS"), ip);
    conexiones.add(c);
    emitter.onCompletion(() -> conexiones.remove(c));
    emitter.onTimeout(() -> {
      conexiones.remove(c);
      emitter.complete();
    });
    emitter.onError(e -> conexiones.remove(c));
    try {
      emitter.send(SseEmitter.event().name("RECUPERAR").data(Map.of("accion", "REST")));
    } catch (Exception e) {
      conexiones.remove(c);
      emitter.complete();
    }
    return emitter;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void confirmado(CambioOperativo cambio) {
    long id = secuencia.incrementAndGet();
    for (Conexion c : conexiones)
      if (permitido(cambio.tipo(), c.roles()))
        try {
          c.emitter().send(SseEmitter.event().id(Long.toString(id)).name(cambio.tipo()).data(cambio));
        } catch (Exception e) {
          conexiones.remove(c);
          c.emitter().complete();
        }
  }

  private void latido() {
    for (Conexion c : conexiones)
      try {
        c.emitter().send(SseEmitter.event().comment("heartbeat"));
      } catch (Exception e) {
        conexiones.remove(c);
        c.emitter().complete();
      }
  }

  @PreDestroy
  public void cerrar() {
    reloj.shutdownNow();
    conexiones.forEach(c -> c.emitter().complete());
    conexiones.clear();
  }
}
