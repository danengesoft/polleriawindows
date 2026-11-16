import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ApiService } from '../../../core/services/api-service';
import { Producto, ProductoRequest } from './producto.model';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/productos';
  cargarImagen(archivo: File) { const datos = new FormData(); datos.append('archivo', archivo); return this.http.post<{ imagenUrl: string }>(this.api.buildUrl(this.resource + '/imagenes'), datos); }
  listar(categoriaId?: number) {
    let params = new HttpParams();
    if (categoriaId !== undefined && categoriaId > 0)
      params = params.set('categoriaId', categoriaId);
    return this.http.get<Producto[]>(this.api.buildUrl(this.resource), { params });
  }
  obtener(id: number) {
    return this.http.get<Producto>(this.api.buildUrl(`${this.resource}/${id}`));
  }
  crear(data: ProductoRequest) {
    return this.http.post<Producto>(this.api.buildUrl(this.resource), data);
  }
  actualizar(id: number, data: ProductoRequest) {
    return this.http.put<Producto>(this.api.buildUrl(`${this.resource}/${id}`), data);
  }
  eliminar(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.resource}/${id}`));
  }
}
