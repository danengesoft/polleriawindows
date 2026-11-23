import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmarEliminacion } from '../../shared/confirmar-eliminacion';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Clinica } from './clinica.model';

@Component({
  selector: 'app-clinica-list',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './clinica-list.html',
})
export class ClinicaList {
  private readonly service = inject(ClinicaService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly datos = signal<Clinica[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly aviso = signal((history.state?.['aviso'] as string) || '');

  constructor() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.service
      .listarClinicas()
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

  eliminar(item: Clinica) {
    this.dialog
      .open(ConfirmarEliminacion, { data: item.razonSocial, width: '26rem', maxWidth: 'calc(100vw - 2rem)' })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        if (confirmado !== true) return;
        this.error.set('');
        this.aviso.set('');
        this.service
          .eliminarClinica(item.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.aviso.set('Clínica eliminada.');
              this.cargar();
            },
            error: (err) => this.error.set(mensajeHttp(err)),
          });
      });
  }
}
