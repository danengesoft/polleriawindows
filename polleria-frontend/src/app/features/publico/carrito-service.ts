import { Injectable, computed, signal } from '@angular/core';
import { Producto, Presentacion } from '../catalogo/producto/producto.model';
export interface LineaCarrito {
  productoId: number;
  presentacionId?: number;
  nombrePresentacion?: string;
  promocionId?: string;
  precioRegular?: number;
  nombre: string;
  precio: number;
  cantidad: number;
}
export const CARRITO_KEY = 'polleria_carrito';
@Injectable({ providedIn: 'root' })
export class CarritoService {
  readonly entregaElegida = signal<'MESA' | 'PARA_LLEVAR' | null>(null);
  private readonly estado = signal<LineaCarrito[]>(this.restaurar());
  readonly lineas = this.estado.asReadonly();
  readonly unidades = computed(() => this.lineas().reduce((n, l) => n + l.cantidad, 0));
  readonly total = computed(
    () => this.lineas().reduce((n, l) => n + Math.round(l.precio * 100) * l.cantidad, 0) / 100,
  );
  agregar(p: Producto, seleccion?: Presentacion) {
    const opciones = p.presentaciones?.filter(x => x.disponible) || [];
    if (!seleccion && opciones.length > 1) return;
    const presentacion = seleccion || opciones[0];
    if (presentacion && !presentacion.disponible) return;
    if (!p.disponible) return;
    const actual = this.lineas().find((l) => l.productoId === p.id && !l.promocionId && l.presentacionId === presentacion?.id);
    if (actual) this.cantidad(p.id, actual.cantidad + 1, undefined, presentacion?.id);
    else
      this.guardar([
        ...this.lineas(),
        { productoId: p.id, ...(presentacion ? { presentacionId: presentacion.id, nombrePresentacion: presentacion.nombre } : {}), nombre: p.nombre, precio: presentacion?.precio ?? p.precio, cantidad: 1 },
      ]);
  }
  agregarPromocion(p: Producto, oferta: { id?: string; precioPromocional?: number; precioRegular?: number; presentacionId?: number }) { if (!p.disponible || !oferta.id || !oferta.precioPromocional) return; const actual = this.lineas().find(l => l.productoId === p.id && l.promocionId === oferta.id); if (actual) this.cantidad(p.id, actual.cantidad + 1, oferta.id, oferta.presentacionId); else this.guardar([...this.lineas(), { productoId: p.id, nombre: p.nombre, precio: oferta.precioPromocional, precioRegular: oferta.precioRegular || p.precio, presentacionId: oferta.presentacionId, nombrePresentacion: p.presentaciones?.find(x => x.id === oferta.presentacionId)?.nombre, promocionId: oferta.id, cantidad: 1 }]); }
  cantidad(id: number, cantidad: number, promocionId?: string, presentacionId?: number) {
    if (!Number.isSafeInteger(cantidad) || cantidad < 1 || cantidad > 999) return;
    this.guardar(this.lineas().map((l) => (l.productoId === id && l.promocionId === promocionId && l.presentacionId === presentacionId ? { ...l, cantidad } : l)));
  }
  eliminar(id: number, promocionId?: string, presentacionId?: number) {
    this.guardar(this.lineas().filter((l) => l.productoId !== id || l.promocionId !== promocionId || l.presentacionId !== presentacionId));
  }
  vaciar() {
    this.guardar([]);
  }
  private guardar(lineas: LineaCarrito[]) {
    this.estado.set(lineas);
    try {
      sessionStorage.setItem(CARRITO_KEY, JSON.stringify(lineas));
    } catch {
      /* Memory remains usable when storage is unavailable. */
    }
  }
  private restaurar(): LineaCarrito[] {
    try {
      const value: unknown = JSON.parse(sessionStorage.getItem(CARRITO_KEY) || '[]');
      if (!Array.isArray(value) || value.length > 100) return [];
      const ids = new Set<string>();
      return value.filter((l): l is LineaCarrito => {
        if (
          !l ||
          !Number.isSafeInteger(l.productoId) ||
          l.productoId < 1 ||
          typeof l.nombre !== 'string' ||
          l.nombre.length > 200 ||
          !Number.isFinite(l.precio) ||
          l.precio <= 0 ||
          !Number.isSafeInteger(l.cantidad) ||
          l.cantidad < 1 ||
          l.cantidad > 999 ||
          ids.has(l.productoId + ':' + (l.presentacionId || '') + ':' + (l.promocionId || ''))
        )
          return false;
        if (l.presentacionId !== undefined && (!Number.isSafeInteger(l.presentacionId) || l.presentacionId <= 0)) return false;
        if (l.promocionId !== undefined && (typeof l.promocionId !== 'string' || !l.promocionId.match(/^[a-f0-9-]{36}$/i))) return false;
        ids.add(l.productoId + ':' + (l.presentacionId || '') + ':' + (l.promocionId || ''));
        return true;
      });
    } catch {
      return [];
    }
  }
}
