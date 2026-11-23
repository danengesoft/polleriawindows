import { Component, inject, signal } from '@angular/core';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Especialidad } from './clinica.model';

@Component({
  selector: 'app-especialidad-list',
  template: `
    <h1>Especialidades</h1>
    @if (error()) { <p role="alert" class="error">{{ error() }}</p> }
    @if (loading()) { <p role="status">Cargando especialidades…</p> }
    <div class="table-region">
      <table class="data-table">
        <caption class="sr-only">Especialidades precargadas</caption>
        <thead><tr><th scope="col">Código</th><th scope="col">Nombre</th></tr></thead>
        <tbody>
          @for (e of datos(); track e.id) {
            <tr><td data-label="Código">{{ e.codigo }}</td><td data-label="Nombre">{{ e.nombre }}</td></tr>
          } @empty {
            @if (!loading() && !error()) { <tr><td class="empty" colspan="2">No hay especialidades.</td></tr> }
          }
        </tbody>
      </table>
    </div>
  `,
})
export class EspecialidadList {
  private readonly service = inject(ClinicaService);
  readonly datos = signal<Especialidad[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  constructor() {
    this.service.listarEspecialidades().subscribe({
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
}
