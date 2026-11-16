import { inject, Injectable } from '@angular/core';
import { OPTIONAL_AUTH } from '../../core/interceptors/auth-interceptor';
import { HttpClient, HttpContext } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import { Cliente } from '../clientes/cliente/cliente.model';
import { PedidoRequest, PedidoResponse } from '../pedidos/pedido.model';
export type PerfilRequest = Omit<Cliente, 'id' | 'activo'>;
export type MiPedidoRequest = Omit<PedidoRequest, 'clienteId'>;
@Injectable({ providedIn: 'root' })
export class ClientePublicoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  perfil(opcional = false) {
    return this.http.get<Cliente>(this.api.buildUrl('/api/v1/clientes/me'), { context: new HttpContext().set(OPTIONAL_AUTH, opcional) });
  }
  guardarPerfil(r: PerfilRequest, existe: boolean) {
    const url = this.api.buildUrl('/api/v1/clientes/me');
    return existe ? this.http.put<Cliente>(url, r) : this.http.post<Cliente>(url, r);
  }
  crearPedido(r: MiPedidoRequest) {
    return this.http.post<PedidoResponse>(this.api.buildUrl('/api/v1/pedidos/mios'), r);
  }
  pedidos() {
    return this.http.get<PedidoResponse[]>(this.api.buildUrl('/api/v1/pedidos/mios'));
  }
  pedido(id: number) {
    return this.http.get<PedidoResponse>(this.api.buildUrl('/api/v1/pedidos/mios/' + id));
  }
  cancelar(id: number) {
    return this.http.patch<PedidoResponse>(
      this.api.buildUrl('/api/v1/pedidos/mios/' + id + '/cancelar'),
      {},
    );
  }
}
