import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { mensajeHttp } from '../../shared/http-error';
import { precioMonetario, textoObligatorio } from '../../shared/form-validation';
import { ClinicaService } from './clinica-service';
import { Clinica, ConsultorioRequest, ESTADOS_CONSULTORIO, Especialidad, EstadoConsultorio } from './clinica.model';

/**
 * Cabecera con detalles: el formulario reactivo es la cabecera (clínica, código,
 * tarifa y estado) y la señal `asignadas` son las líneas (especialidades).
 * Al guardar se envía todo junto en una sola petición.
 */
@Component({
  selector: 'app-consultorio-form',
  imports: [ReactiveFormsModule, FormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './consultorio-form.html',
  styles: `.lineas{grid-column:1/-1;border:1px solid var(--mat-sys-outline-variant,#ccc);border-radius:8px;padding:1rem;display:grid;gap:.75rem}
    .lineas legend{font-weight:600;padding:0 .25rem}
    .agregar-linea{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center}
    .agregar-linea mat-form-field{flex:1 1 14rem}`,
})
export class ConsultorioForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClinicaService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly estados = ESTADOS_CONSULTORIO;
  readonly clinicas = signal<Clinica[]>([]);
  readonly especialidades = signal<Especialidad[]>([]);
  readonly asignadas = signal<Especialidad[]>([]); // las líneas
  readonly elegidaId = signal<number | null>(null); // especialidad en la lista desplegable
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly errorLineas = signal('');

  // Solo se ofrecen las especialidades que aún no se agregaron.
  readonly disponibles = computed(() => {
    const usadas = new Set(this.asignadas().map((e) => e.id));
    return this.especialidades().filter((e) => !usadas.has(e.id));
  });

  readonly form = this.fb.group({
    clinicaId: this.fb.control<number | null>(null, Validators.required),
    codigo: this.fb.nonNullable.control('', [textoObligatorio, Validators.maxLength(10)]),
    tarifaHora: this.fb.nonNullable.control('', [precioMonetario]),
    estado: this.fb.nonNullable.control<EstadoConsultorio>('DISPONIBLE', Validators.required),
  });

  constructor() {
    this.service.listarClinicas().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.clinicas.set(data.filter((c) => c.activo)),
      error: (err) => this.error.set(mensajeHttp(err)),
    });
    this.service.listarEspecialidades().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.especialidades.set(data),
      error: (err) => this.error.set(mensajeHttp(err)),
    });
  }

  agregar() {
    const id = this.elegidaId();
    const especialidad = this.especialidades().find((e) => e.id === id);
    if (!especialidad) {
      this.errorLineas.set('Elige una especialidad de la lista.');
      return;
    }
    this.asignadas.update((lista) => [...lista, especialidad]);
    this.elegidaId.set(null);
    this.errorLineas.set('');
  }

  quitar(especialidad: Especialidad) {
    this.asignadas.update((lista) => lista.filter((e) => e.id !== especialidad.id));
  }

  mensaje(campo: 'clinicaId' | 'codigo' | 'tarifaHora'): string {
    const control = this.form.controls[campo];
    if (!control.touched || !control.errors) return '';
    if (campo === 'clinicaId') return 'Elige la clínica';
    if (campo === 'codigo') return control.errors['maxlength'] ? 'Máximo 10 caracteres' : 'El código es obligatorio';
    return 'Ingresa una tarifa mayor que 0, con hasta 2 decimales';
  }

  guardar() {
    if (this.guardando()) return;
    this.error.set('');
    const sinLineas = this.asignadas().length === 0;
    this.errorLineas.set(sinLineas ? 'Agrega al menos una especialidad.' : '');
    if (this.form.invalid || sinLineas) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const data: ConsultorioRequest = {
      clinicaId: v.clinicaId!,
      codigo: v.codigo.trim().toUpperCase(),
      tarifaHora: Number(v.tarifaHora),
      estado: v.estado,
      especialidadIds: this.asignadas().map((e) => e.id),
    };
    this.guardando.set(true);
    this.service
      .crearConsultorio(data)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (c) =>
          this.router.navigate(['/interno/clinica/consultorios'], {
            state: { aviso: `Consultorio ${c.codigo} registrado con ${c.especialidades.length} especialidad(es).` },
          }),
        error: (err) => {
          this.guardando.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
  }
}
