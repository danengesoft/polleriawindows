import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { FotoProducto } from './foto-producto';
import { IconoCliente } from './icono-cliente';
import { ProductoDetallePublico } from './producto-detalle';
import { Component, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService } from '../catalogo/producto/producto-service';
import { CategoriaService } from '../catalogo/categoria/categoria-service';
import { Producto } from '../catalogo/producto/producto.model';
import { Categoria } from '../catalogo/categoria/categoria.model';
import { CarritoService } from './carrito-service';
import { mensajeHttp } from '../../shared/http-error';
@Component({
  selector: 'app-catalogo-publico',
  imports: [CurrencyPipe, RouterLink, FotoProducto, IconoCliente],
  styleUrl: './publico.css',
  templateUrl: './catalogo-publico.html',
})
export class CatalogoPublico {
  private readonly dialog = inject(MatDialog);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly viendoProductos = signal(false);
  abrirCategoria(id = '') { this.router.navigate(['/catalogo'], { queryParams: id ? { categoria: id } : { todos: '1' } }); }
  readonly categoriaSeleccionada = signal('');
  readonly busqueda = signal('');
  readonly filtrados = computed(() => { const normalizar = (v: string) => v.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLocaleLowerCase('es-PE'); const q = normalizar(this.busqueda().trim()); return this.productos().filter(p => !q || normalizar(p.nombre + ' ' + (p.descripcion || '')).includes(q)); });
  private readonly productosApi = inject(ProductoService);
  private readonly categoriasApi = inject(CategoriaService);
  readonly carrito = inject(CarritoService);
  readonly imagenesFallidas = signal<Set<number>>(new Set());
  imagenFallida(id: number) {
    this.imagenesFallidas.update((ids) => new Set([...ids, id]));
  }
  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly error = signal('');
  readonly aviso = signal('');
  readonly cargando = signal(false);
  constructor() {
    this.categoriasApi.listar().subscribe({
      next: (c) => this.categorias.set(c.filter((categoria) => categoria.activo)),
      error: (e) => this.error.set(mensajeHttp(e)),
    });
    this.route.queryParamMap.subscribe((p) => {
      this.busqueda.set((p.get('q') || '').slice(0, 120));
      const id = p.get('categoria') || '';
      this.viendoProductos.set(!!id || p.get('todos') === '1' || !!p.get('q'));
      this.cargar(/^[1-9][0-9]*$/.test(id) ? id : '');
    });
  }
  cargar(id = '') {
    this.categoriaSeleccionada.set(id);
    this.cargando.set(true);
    this.error.set('');
    this.productosApi.listar(id ? Number(id) : undefined).subscribe({
      next: (p) => {
        this.productos.set(p.filter((producto) => producto.disponible === true));
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeHttp(e));
        this.cargando.set(false);
      },
    });
  }
  verDetalle(p: Producto) {
    this.dialog
      .open(ProductoDetallePublico, {
        data: p,
        width: '600px',
        maxWidth: '94vw',
        maxHeight: '92dvh',
        autoFocus: 'first-tabbable',
        restoreFocus: true,
      })
      .afterClosed()
      .subscribe((agregado) => {
        if (agregado) this.aviso.set(p.nombre + ' agregado al carrito.');
      });
  }
  agregar(p: Producto) {
    if ((p.presentaciones?.filter(x => x.disponible).length || 0) > 1) { this.verDetalle(p); return; }
    this.carrito.agregar(p);
    this.aviso.set(p.nombre + ' agregado al carrito.');
  }
  imagen(url: string | null) {
    if (!url) return null;
    try {
      const u = new URL(url);
      return ['http:', 'https:'].includes(u.protocol) ? u.href : null;
    } catch {
      return null;
    }
  }
}
