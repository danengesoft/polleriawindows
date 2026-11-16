import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../../core/services/api-service';
import { Cliente, ClienteRequest } from './cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/clientes';
  listar() {
    return this.http.get<Cliente[]>(this.api.buildUrl(this.resource));
  }
  obtener(id: number) {
    return this.http.get<Cliente>(this.api.buildUrl(`${this.resource}/${id}`));
  }
  crear(data: ClienteRequest) {
    return this.http.post<Cliente>(this.api.buildUrl(this.resource), data);
  }
  actualizar(id: number, data: ClienteRequest) {
    return this.http.put<Cliente>(this.api.buildUrl(`${this.resource}/${id}`), data);
  }
  eliminar(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
