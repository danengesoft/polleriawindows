import { RegistroDetalle } from '../../../shared/registro-detalle';
import { AuthService } from '../../../core/auth/auth-service';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmarEliminacion } from '../../../shared/confirmar-eliminacion';
import { mensajeHttp } from '../../../shared/http-error';
import { CategoriaService } from './categoria-service';
import { Categoria } from './categoria.model';

@Component({
  selector: 'app-categoria-list',
  imports: [RegistroDetalle, RouterLink, MatButtonModule],
  templateUrl: './categoria-list.html',
})
export class CategoriaList {
  readonly auth = inject(AuthService);
  private readonly service = inject(CategoriaService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly seleccionado = signal<Categoria | null>(null);
  readonly busqueda = signal('');
  readonly filtrados = computed(() => { const texto = this.busqueda().trim().toLocaleLowerCase('es-PE'); return this.datos().filter(item => Object.values(item).filter(v => typeof v === 'string').join(' ').toLocaleLowerCase('es-PE').includes(texto)); });
  readonly datos = signal<Categoria[]>([]);
  readonly loading = signal(false);
  readonly eliminando = signal<number | null>(null);
  readonly error = signal('');
  readonly aviso = signal((history.state?.['aviso'] as string) || '');

  constructor() {
    this.cargar();
  }
  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.service
      .listar()
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
  eliminar(item: Categoria) {
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
  private eliminarConfirmado(item: Categoria) {
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
