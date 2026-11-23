import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Consultorio } from './clinica.model';

// Solo lectura: la cabecera y su tabla de especialidades asignadas.
@Component({
  selector: 'app-consultorio-detalle',
  imports: [RouterLink, MatButtonModule, DecimalPipe],
  template: `
    @if (error()) { <p role="alert" class="error">{{ error() }}</p> }
    @if (consultorio(); as c) {
      <h1>Consultorio {{ c.codigo }}</h1>
      <dl class="detalle-cabecera">
        <div><dt>Clínica</dt><dd>{{ c.clinicaRazonSocial }}</dd></div>
        <div><dt>Tarifa por hora</dt><dd>{{ c.tarifaHora | number: '1.2-2' }}</dd></div>
        <div><dt>Estado</dt><dd>{{ c.estado }}</dd></div>
      </dl>
      <h2>Especialidades asignadas</h2>
      <table class="data-table">
        <caption class="sr-only">Especialidades del consultorio {{ c.codigo }}</caption>
        <thead><tr><th scope="col">Código</th><th scope="col">Especialidad</th></tr></thead>
        <tbody>
          @for (e of c.especialidades; track e.id) {
            <tr><td data-label="Código">{{ e.codigo }}</td><td data-label="Especialidad">{{ e.nombre }}</td></tr>
          } @empty {
            <tr><td class="empty" colspan="2">Sin especialidades asignadas.</td></tr>
          }
        </tbody>
      </table>
    } @else if (!error()) {
      <p role="status">Cargando consultorio…</p>
    }
    <div class="form-actions"><a matButton routerLink="/interno/clinica/consultorios">Volver</a></div>
  `,
  styles: `.detalle-cabecera{display:grid;grid-template-columns:repeat(auto-fit,minmax(12rem,1fr));gap:.75rem;margin:1rem 0}
    .detalle-cabecera dt{font-weight:600}.detalle-cabecera dd{margin:0}`,
})
export class ConsultorioDetalle {
  private readonly service = inject(ClinicaService);
  readonly consultorio = signal<Consultorio | null>(null);
  readonly error = signal('');

  constructor() {
    const id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
    this.service.obtenerConsultorio(id).subscribe({
      next: (data) => this.consultorio.set(data),
      error: (err) => this.error.set(mensajeHttp(err)),
    });
  }
}
