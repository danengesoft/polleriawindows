import { RegistroDetalle } from '../../../shared/registro-detalle';
import { AuthService } from '../../../core/auth/auth-service';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmarEliminacion } from '../../../shared/confirmar-eliminacion';
import { mensajeHttp } from '../../../shared/http-error';
import { ProductoService } from './producto-service';
import { Producto } from './producto.model';
import { CategoriaService } from '../categoria/categoria-service';
import { Categoria } from '../categoria/categoria.model';
import { CurrencyPipe } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-producto-list',
  imports: [RegistroDetalle, RouterLink, MatButtonModule, CurrencyPipe, MatFormFieldModule, MatSelectModule],
  templateUrl: './producto-list.html',
})
export class ProductoList {
  readonly auth = inject(AuthService);
  private readonly service = inject(ProductoService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly seleccionado = signal<Producto | null>(null);
  readonly busqueda = signal('');
  readonly filtrados = computed(() => { const texto = this.busqueda().trim().toLocaleLowerCase('es-PE'); return this.datos().filter(item => Object.values(item).filter(v => typeof v === 'string').join(' ').toLocaleLowerCase('es-PE').includes(texto)); });
  readonly datos = signal<Producto[]>([]);
  readonly loading = signal(false);
  readonly eliminando = signal<number | null>(null);
  readonly error = signal('');
  readonly aviso = signal((history.state?.['aviso'] as string) || '');
  private readonly categoriaService = inject(CategoriaService);
  readonly categorias = signal<Categoria[]>([]);
  readonly errorCategorias = signal('');
  readonly categoriaFiltro = signal<number | null>(null);
  private consulta?: Subscription;
  filtrar(id: number | null) {
    this.categoriaFiltro.set(id);
    this.cargar();
  }

  constructor() {
    this.categoriaService
      .listar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => this.categorias.set(data),
        error: (err) => this.errorCategorias.set(mensajeHttp(err)),
      });
    this.cargar();
  }
  cargar() {
    this.consulta?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.consulta = this.service
      .listar(this.categoriaFiltro() ?? undefined)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.datos.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(mensajeHttp(err));
          this.loading.set(false);
        },
      });
  }
  eliminar(item: Producto) {
    if (this.eliminando() !== null) return;
    this.dialog
      .open(ConfirmarEliminacion, {
        data: item.nombre + ' · Se retirará de la web y de los listados activos. El historial de pedidos se conserva.',
        width: '26rem',
        maxWidth: 'calc(100vw - 2rem)',
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        if (confirmado === true) this.eliminarConfirmado(item);
      });
  }
  private eliminarConfirmado(item: Producto) {
    if (this.eliminando() !== null) return;
    this.error.set('');
    this.aviso.set('');
    this.eliminando.set(item.id);
    this.service
      .eliminar(item.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.eliminando.set(null);
          this.seleccionado.set(null); this.aviso.set('Registro eliminado de la web. El historial se conserva.');
          this.cargar();
        },
        error: (err) => {
          this.eliminando.set(null);
          if (err.status === 404) {
            this.datos.update((data) => data.filter((row) => row.id !== item.id));
          }
          this.error.set(mensajeHttp(err));
        },
      });
  }
}
