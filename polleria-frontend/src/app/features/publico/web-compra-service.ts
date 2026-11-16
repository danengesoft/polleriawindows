import type { PagoPrueba } from './pago-prueba';
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { firstValueFrom, from } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth/auth-service';
import { PedidoResponse, PedidoRequest } from '../pedidos/pedido.model';
export type PedidoSeguimiento = Pick<PedidoResponse, 'id' | 'flujoVersion' | 'fecha' | 'tipoPedido' | 'estado' | 'estadoPago' | 'metodoPago' | 'total' | 'mesaNumero' | 'detalles'> & { direccionEntrega?: string | null; referenciaEntrega?: string | null };
export interface CompradorWeb { nombres: string; apellidos: string; telefono: string; correo: string | null }
export type CompraWeb = Omit<PedidoRequest, 'clienteId'> & { comprador: CompradorWeb };
const CLAVE = 'polleria_web_confirmacion';
@Injectable({ providedIn: 'root' })
export class WebCompraService {
    private http = inject(HttpClient); private auth = inject(AuthService); private base = environment.apiBaseUrl + '/api/v1/web'; private sesion?: Promise<{ csrf: string }>;
    private iniciar() { return this.sesion ??= firstValueFrom(this.http.post<{ csrf: string }>(this.base + '/sesion', {}, { withCredentials: true })).catch(e => { this.sesion = undefined; throw e; }); }
    private async cabeceras() { return new HttpHeaders({ 'X-Web-CSRF': (await this.iniciar()).csrf }); }
    private clave() { let clave = sessionStorage.getItem(CLAVE); if (!clave) { clave = crypto.randomUUID(); sessionStorage.setItem(CLAVE, clave); } return clave; }
    crear(datos: CompraWeb) {
        return from((async () => {
            let headers = (await this.cabeceras()).set('Idempotency-Key', this.clave());
            if (this.auth.usuario() && this.auth.tieneRol('CLIENTE')) {
                if (!await this.auth.asegurarSesion()) throw Error('Tu sesión personal venció. Revisa tus datos y vuelve a confirmar sin cuenta, o inicia sesión voluntariamente.');
                const token = this.auth.accessToken(); if (token) headers = headers.set('Authorization', 'Bearer ' + token);
            }
            const p = await firstValueFrom(this.http.post<PedidoSeguimiento>(this.base + '/pedidos', datos, { headers, withCredentials: true })); sessionStorage.removeItem(CLAVE); return p;
        })());
    }
    pagoPruebaInterno(id: number, resultado: string) { return this.http.post<PagoPrueba>(environment.apiBaseUrl + '/api/v1/pedidos/' + id + '/pago-prueba', { resultado }); }
    pagoPrueba(id: number, resultado: string) { return from((async () => firstValueFrom(this.http.post<PagoPrueba>(this.base + '/pedidos/' + id + '/pago-prueba', { resultado }, { headers: await this.cabeceras(), withCredentials: true })))()); }
    seguir(id: number) { return this.http.get<PedidoSeguimiento>(this.base + '/pedidos/' + id, { withCredentials: true }); }
    generarClave(id: number) { return from((async () => firstValueFrom(this.http.post<{ clave: string; expira: string }>(this.base + '/pedidos/' + id + '/clave', {}, { headers: await this.cabeceras(), withCredentials: true })))()); }
    recuperar(id: number, clave: string) { return from((async () => firstValueFrom(this.http.post<PedidoSeguimiento>(this.base + '/pedidos/' + id + '/recuperar', { clave }, { headers: await this.cabeceras(), withCredentials: true })))()); }
    revocar(id: number) { return from((async () => firstValueFrom(this.http.post<void>(this.base + '/pedidos/' + id + '/revocar', {}, { headers: await this.cabeceras(), withCredentials: true })))()); }
}
