import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
export interface OfertaPromocion { id?: string; productoId: number; presentacionId?: number; tipo: 'PORCENTAJE' | 'PRECIO'; valor: number; precioRegular?: number; precioPromocional?: number; porcentaje?: number }
export interface SeleccionPromociones { productoIds: number[]; ofertas?: OfertaPromocion[] }
@Injectable({ providedIn: 'root' })
export class PromocionesService {
    private http = inject(HttpClient); private api = inject(ApiService);
    consultar() { return this.http.get<SeleccionPromociones>(this.api.buildUrl('/api/v1/promociones')); }
    guardar(productoIds: number[], ofertas?: OfertaPromocion[]) { return this.http.put<SeleccionPromociones>(this.api.buildUrl('/api/v1/promociones'), { productoIds, ...(ofertas ? { ofertas } : {}) }); }
}
