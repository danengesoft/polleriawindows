package pe.edu.upeu.polleria.pedidos.eventos;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/eventos")
public class EventosController {
    private final EventosService service;

    @GetMapping(produces = "text/event-stream")
    public SseEmitter conectar() {
        return service.conectar();
    }
}
