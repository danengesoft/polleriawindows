import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../../core/services/api-service';
import { Categoria, CategoriaRequest } from './categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/categorias';
  listar() {
    return this.http.get<Categoria[]>(this.api.buildUrl(this.resource));
  }
  obtener(id: number) {
    return this.http.get<Categoria>(this.api.buildUrl(`${this.resource}/${id}`));
  }
  crear(data: CategoriaRequest) {
    return this.http.post<Categoria>(this.api.buildUrl(this.resource), data);
  }
  actualizar(id: number, data: CategoriaRequest) {
    return this.http.put<Categoria>(this.api.buildUrl(`${this.resource}/${id}`), data);
  }
  eliminar(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
