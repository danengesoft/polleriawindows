import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import { ProductoService } from '../catalogo/producto/producto-service';
import { CategoriaService } from '../catalogo/categoria/categoria-service';
import { Producto } from '../catalogo/producto/producto.model';
import { forkJoin } from 'rxjs';
import { mensajeHttp } from '../../shared/http-error';
import { Injectable } from '@angular/core';
@Injectable({ providedIn: 'root' })
export class DestacadosService {
    private http = inject(HttpClient); private api = inject(ApiService);
    consultar() { return this.http.get<{ productoIds: number[]; configurada?: boolean }>(this.api.buildUrl('/api/v1/destacados')); }
    guardar(productoIds: number[]) { return this.http.put<{ productoIds: number[] }>(this.api.buildUrl('/api/v1/destacados'), { productoIds, configurada: true }); }
    imagen(url: string | null): string | null { if (!url) return null; if (url.startsWith('/api/v1/productos/imagenes/')) return this.api.buildUrl(url); try { const u = new URL(url); return ['http:', 'https:'].includes(u.protocol) ? u.href : null; } catch { return null; } }
}
@Component({
    selector: 'app-destacados-admin', imports: [CurrencyPipe], template: `
<h2>Destacados de la carta</h2><p>Selecciona hasta tres productos registrados. Se utilizarán su fotografía, nombre y precio actuales. Para cambiar una foto, edita el producto.</p>
@if(error()){<p role="alert">{{error()}}</p>}@if(aviso()){<p role="status">{{aviso()}}</p>}
<p>{{ids().length}} de 3 seleccionados</p><button type="button" [disabled]="!listo()||guardando()" (click)="guardar()">Guardar destacados</button>
<div class="grid">@for(p of productos();track p.id){<button type="button" class="card" [class.selected]="ids().includes(p.id)" [attr.aria-pressed]="ids().includes(p.id)" (click)="alternar(p.id)" [disabled]="guardando()||(!ids().includes(p.id)&&ids().length>=3)">@if(api.imagen(p.imagenUrl);as foto){<img [src]="foto" [alt]="p.nombre"/>}@else{<span class="missing">Sin imagen registrada</span>}<strong>{{p.nombre}}</strong><span>{{p.precio|currency:'PEN':'S/ ':'1.2-2'}}</span><span>{{ids().includes(p.id)?'Seleccionado · '+(ids().indexOf(p.id)+1):'Seleccionar'}}</span></button>}@empty{<p>No hay productos disponibles en categorías activas.</p>}</div>
`, styles: `.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:16px;margin-top:20px}.card{display:flex;flex-direction:column;gap:10px;padding:16px;border:1px solid #45494d;background:#24282b;color:#f5eee5;border-radius:12px;text-align:left}.selected{border:2px solid #ed334e;background:#43272e}img,.missing{height:120px;width:100%;object-fit:contain}.missing{display:grid;place-items:center}button:disabled{opacity:.6}`
})
export class DestacadosAdmin {
    readonly api = inject(DestacadosService); private productosApi = inject(ProductoService); private categoriasApi = inject(CategoriaService);
    readonly productos = signal<Producto[]>([]); readonly ids = signal<number[]>([]); readonly listo = signal(false); readonly guardando = signal(false); readonly error = signal(''); readonly aviso = signal('');
    constructor() { forkJoin({ p: this.productosApi.listar(), c: this.categoriasApi.listar(), d: this.api.consultar() }).subscribe({ next: r => { const productos = r.p.filter(p => p.disponible && r.c.some(c => c.id === p.categoria.id && c.activo)); this.productos.set(productos); this.ids.set(r.d.configurada === false ? productos.slice(0, 3).map(p => p.id) : r.d.productoIds.filter(id => productos.some(p => p.id === id))); this.listo.set(true); }, error: e => this.error.set(mensajeHttp(e)) }); }
    alternar(id: number) { this.ids.update(ids => ids.includes(id) ? ids.filter(v => v !== id) : ids.length < 3 ? [...ids, id] : ids); this.aviso.set(''); }
    guardar() { if (!this.listo() || this.guardando()) return; this.guardando.set(true); this.error.set(''); this.api.guardar(this.ids()).subscribe({ next: r => { this.ids.set(r.productoIds); this.guardando.set(false); this.aviso.set('Destacados guardados. Ya están disponibles en el inicio.'); }, error: e => { this.guardando.set(false); this.error.set(mensajeHttp(e)); } }); }
}
