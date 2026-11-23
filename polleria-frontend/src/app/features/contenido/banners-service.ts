import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
export interface Banner { id: string; titulo: string; imagenUrl: string; imagenMovilUrl: string | null; activo: boolean }
export const BANNER_ORIGINAL: Banner = { id: '00000000-0000-0000-0000-000000000001', titulo: 'Pollos y parrillas Pollería Neyos', imagenUrl: '/images/polleria-neyos/inicio-hero.png', imagenMovilUrl: null, activo: true };
@Injectable({ providedIn: 'root' })
export class BannersService {
    private http = inject(HttpClient); private api = inject(ApiService);
    publicos() { return this.http.get<{ banners: Banner[] }>(this.api.buildUrl('/api/v1/banners')); }
    gestion() { return this.http.get<{ banners: Banner[] }>(this.api.buildUrl('/api/v1/banners/gestion')); }
    guardar(banners: Banner[]) { return this.http.put<{ banners: Banner[] }>(this.api.buildUrl('/api/v1/banners'), { banners }); }
    imagen(ruta: string) { return ruta.startsWith('/api/') ? this.api.buildUrl(ruta) : ruta; }
}
