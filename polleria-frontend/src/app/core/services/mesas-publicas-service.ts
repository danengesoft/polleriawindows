import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
@Injectable({ providedIn: 'root' })
export class MesasPublicasService {
    readonly revision = signal(0); private usuarios = 0; private controller?: AbortController; private timer?: ReturnType<typeof setTimeout>;
    conectar() { this.usuarios++; if (this.usuarios === 1) this.iniciar(); return () => { this.usuarios--; if (!this.usuarios) { this.controller?.abort(); clearTimeout(this.timer); } }; }
    private iniciar() { const c = new AbortController(); this.controller = c; void this.leer(c); }
    private async leer(c: AbortController) { try { const r = await fetch(environment.apiBaseUrl + '/api/v1/web/mesas/eventos', { signal: c.signal }); if (!r.ok || !r.body) throw Error(); this.revision.update(n => n + 1); const reader = r.body.getReader(), decoder = new TextDecoder(); let buffer = ''; while (!c.signal.aborted) { const { done, value } = await reader.read(); if (done) break; buffer += decoder.decode(value, { stream: true }).replaceAll('\r\n', '\n'); let i; while ((i = buffer.indexOf('\n\n')) >= 0) { const data = buffer.slice(0, i); buffer = buffer.slice(i + 2); if (data.includes('data:')) this.revision.update(n => n + 1); } } } catch {/* El mapa recupera el estado por REST, sin credenciales en URL. */ } finally { if (!c.signal.aborted && this.usuarios) this.timer = setTimeout(() => this.iniciar(), 5000); } }
}
