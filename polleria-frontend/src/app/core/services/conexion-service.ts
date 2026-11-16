import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';
import { ApiService } from './api-service';

export interface ResultadoConexion {
  codigoHttp: number;
  traceId: string | null;
}

@Injectable({ providedIn: 'root' })
export class ConexionService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  comprobar() {
    // Solo diagnóstico: no conserva datos de categorías ni expone operaciones CRUD.
    return this.http.get<unknown[]>(this.api.buildUrl('/api/v1/categorias'), { observe: 'response' })
      .pipe(map(response => ({ codigoHttp: response.status, traceId: response.headers.get('X-Trace-ID') })));
  }
}
