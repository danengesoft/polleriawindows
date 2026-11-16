import { Injectable, effect, inject, signal } from '@angular/core';
import { AuthService } from '../auth/auth-service';
import { environment } from '../../../environments/environment';
@Injectable({ providedIn: 'root' })
export class EventosService {
    readonly revision = signal(0); readonly conectado = signal(false);
    private auth = inject(AuthService); private controller?: AbortController; private timer?: ReturnType<typeof setTimeout>;
    constructor() { effect((onCleanup) => { onCleanup(() => this.detener()); const sub = this.auth.usuario()?.sub; this.detener(); if (sub) this.iniciar(); }); }
    private detener() { this.controller?.abort(); clearTimeout(this.timer); this.conectado.set(false); }
    private iniciar() { const c = new AbortController(); this.controller = c; void this.leer(c); }
    private async leer(c: AbortController) {
        let reintentar = true;
        try {
            const r = await fetch(environment.apiBaseUrl + '/api/v1/eventos', { headers: { Authorization: 'Bearer ' + this.auth.accessToken() }, signal: c.signal });
            if (r.status === 403) { reintentar = false; return; }
            if (r.status === 401) { await this.auth.refrescar(); return; }
            if (!r.ok || !r.body) throw Error('SSE no disponible');
            this.conectado.set(true); this.revision.update(v => v + 1);
            const reader = r.body.getReader(); const decoder = new TextDecoder(); let buffer = '';
            while (!c.signal.aborted) {
                const { done, value } = await reader.read(); if (done) break; buffer += decoder.decode(value, { stream: true }).replaceAll('\r\n', '\n'); let end;
                while ((end = buffer.indexOf('\n\n')) >= 0) { const evento = buffer.slice(0, end); buffer = buffer.slice(end + 2); if (evento.includes('data:')) this.revision.update(v => v + 1); }
            }
        } catch {/* La recuperación siempre consulta REST; no se muestran ni registran tokens. */ }
        finally { if (!c.signal.aborted) { this.conectado.set(false); if (reintentar && this.auth.usuario()) this.timer = setTimeout(() => this.iniciar(), 3000); } }
    }
}
