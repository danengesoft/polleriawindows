import { MatDialog } from '@angular/material/dialog';
import { ProductoDetallePublico } from '../../features/publico/producto-detalle';
import { PromocionesService, OfertaPromocion } from '../../features/contenido/promociones-service';
import { DestacadosService } from '../../features/contenido/destacados';
import { BannerInicio } from '../../features/contenido/banner-inicio';
import { Component, computed, inject, signal } from '@angular/core';
import { ClienteShell } from '../layout/publico/cliente-shell';
import { CurrencyPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductoService } from '../../features/catalogo/producto/producto-service';
import { CategoriaService } from '../../features/catalogo/categoria/categoria-service';
import { Producto } from '../../features/catalogo/producto/producto.model';
import { Categoria } from '../../features/catalogo/categoria/categoria.model';
import { CarritoService } from '../../features/publico/carrito-service';
import { AuthService, INTERNOS } from '../auth/auth-service';

@Component({
  selector: 'app-inicio-publico',
  imports: [RouterLink, CurrencyPipe, ClienteShell, BannerInicio],
  templateUrl: './inicio-publico.html',
  styleUrls: [
    './inicio-publico.css',
    './inicio-hero.css',
    './inicio-carta.css',
    './inicio-responsive.css',
    './inicio-carrusel.css',
  ],
})
export class InicioPublico {
  readonly seccion = inject(ActivatedRoute).snapshot.data["seccion"] as "destacados" | "promociones" | undefined;
  private readonly dialog = inject(MatDialog);
  verDetalle(p: Producto, promocion = false) { this.dialog.open(ProductoDetallePublico, { data: { ...p, oferta: promocion ? this.oferta(p.id) : undefined }, width: "600px", maxWidth: "94vw", maxHeight: "92dvh", autoFocus: "first-tabbable", restoreFocus: true }); }
  readonly auth = inject(AuthService);
  readonly carrito = inject(CarritoService);
  private readonly promocionesApi = inject(PromocionesService);
  readonly seleccionPromociones = signal<number[]>([]);
  readonly ofertas = signal<OfertaPromocion[]>([]);
  oferta(id: number) { return this.ofertas().find(o => o.productoId === id); }
  nombrePresentacionOferta(p: Producto) { const id = this.oferta(p.id)?.presentacionId; return p.presentaciones?.find(x => x.id === id)?.nombre || ''; }
  agregarOferta(p: Producto) { const oferta = this.oferta(p.id); if (oferta) this.verDetalle(p, true); }
  readonly errorPromociones = signal(false);
  private readonly destacadosApi = inject(DestacadosService);
  readonly seleccionDestacados = signal<number[] | null>(null);
  private readonly productosApi = inject(ProductoService);
  private readonly categoriasApi = inject(CategoriaService);
  readonly menuAbierto = signal(false);
  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly cargandoProductos = signal(false);
  readonly cargandoCategorias = signal(false);
  readonly errorProductos = signal(false);
  readonly errorCategorias = signal(false);
  readonly imagenesFallidas = signal<Set<string>>(new Set());
  readonly promociones = computed(() => this.seleccionPromociones().map(id => this.productos().find(p => p.id === id)).filter((p): p is Producto => !!p && p.disponible && this.categorias().some(c => c.id === p.categoria.id)));
  readonly destacados = computed(() => {
    const base = (this.seleccionDestacados() === null ? this.productos() : this.seleccionDestacados()!.map(id => this.productos().find(p => p.id === id)).filter((p): p is Producto => !!p))
      .filter(p => p.disponible && this.categorias().some(c => c.id === p.categoria.id)).slice(0, 3);
    return [...base, ...this.promociones().filter(p => !base.some(d => d.id === p.id))];
  });
  readonly portalInterno = computed(() => this.auth.tieneRol(...INTERNOS));

  constructor() {
    this.cargarPromociones();
    this.destacadosApi.consultar().subscribe({ next: r => this.seleccionDestacados.set(r.configurada === false ? null : r.productoIds), error: () => this.seleccionDestacados.set([]) });
    this.cargarProductos();
    this.cargarCategorias();
  }

  cargarPromociones() { this.errorPromociones.set(false); this.promocionesApi.consultar().subscribe({ next: r => { this.seleccionPromociones.set(r.productoIds); this.ofertas.set(r.ofertas || []); }, error: () => this.errorPromociones.set(true) }); }

  cargarProductos() {
    this.cargandoProductos.set(true);
    this.errorProductos.set(false);
    this.productosApi.listar().subscribe({
      next: (p) => {
        this.productos.set(p);
        this.cargandoProductos.set(false);
      },
      error: () => {
        this.errorProductos.set(true);
        this.cargandoProductos.set(false);
      },
    });
  }

  cargarCategorias() {
    this.cargandoCategorias.set(true);
    this.errorCategorias.set(false);
    this.categoriasApi.listar().subscribe({
      next: (c) => {
        this.categorias.set(c.filter((categoria) => categoria.activo));
        this.cargandoCategorias.set(false);
      },
      error: () => {
        this.errorCategorias.set(true);
        this.cargandoCategorias.set(false);
      },
    });
  }

  imagen(url: string | null): string | null {
    if (!url || this.imagenesFallidas().has(url)) return null;
    return this.destacadosApi.imagen(url);
  }

  imagenCategoria(id: number): string | null {
    for (const producto of [...this.promociones(), ...this.productos().filter(p => p.disponible)]) {
      if (producto.categoria.id === id) {
        const imagen = this.imagen(producto.imagenUrl);
        if (imagen) return imagen;
      }
    }
    return null;
  }

  imagenFallida(url: string | null) {
    if (url) this.imagenesFallidas.update((actuales) => new Set([...actuales, url]));
  }

  cerrarMenu(boton?: HTMLButtonElement) {
    this.menuAbierto.set(false);
    boton?.focus();
  }
}
