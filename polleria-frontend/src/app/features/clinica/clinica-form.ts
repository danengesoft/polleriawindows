import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { mensajeHttp } from '../../shared/http-error';
import { textoObligatorio } from '../../shared/form-validation';
import { ClinicaService } from './clinica-service';
import { ClinicaRequest } from './clinica.model';

// La misma pantalla sirve para crear (/nuevo) y editar (/:id/editar).
@Component({
  selector: 'app-clinica-form',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatCheckboxModule],
  templateUrl: './clinica-form.html',
})
export class ClinicaForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClinicaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly id = signal<number | null>(null);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');

  // Validaciones del examen: RUC de 11 dígitos y razón social obligatoria.
  readonly form = this.fb.nonNullable.group({
    ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    razonSocial: ['', [textoObligatorio, Validators.maxLength(150)]],
    telefono: ['', [Validators.maxLength(20)]],
    activo: [true],
  });

  constructor() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param === null) return;
    const id = Number(param);
    this.id.set(id);
    this.loading.set(true);
    this.service
      .obtenerClinica(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (c) => {
          this.form.patchValue({ ruc: c.ruc, razonSocial: c.razonSocial, telefono: c.telefono ?? '', activo: c.activo });
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
  }

  // Mensaje de error visible debajo de cada campo.
  mensaje(campo: 'ruc' | 'razonSocial' | 'telefono'): string {
    const control: AbstractControl = this.form.controls[campo];
    if (!control.touched || !control.errors) return '';
    if (campo === 'ruc') {
      return control.errors['required'] ? 'El RUC es obligatorio' : 'El RUC debe tener 11 dígitos';
    }
    if (campo === 'razonSocial') {
      return control.errors['required'] ? 'La razón social es obligatoria' : 'Máximo 150 caracteres';
    }
    return 'Máximo 20 caracteres';
  }

  guardar() {
    if (this.guardando()) return;
    this.error.set('');
    this.form.controls.ruc.setValue(this.form.controls.ruc.value.trim());
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // muestra todos los errores
      return;
    }
    const v = this.form.getRawValue();
    const data: ClinicaRequest = {
      ruc: v.ruc,
      razonSocial: v.razonSocial.trim(),
      telefono: v.telefono.trim() || null,
      activo: v.activo,
    };
    this.guardando.set(true);
    const id = this.id();
    const peticion = id === null ? this.service.crearClinica(data) : this.service.actualizarClinica(id, data);
    peticion.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () =>
        this.router.navigate(['/interno/clinica/clinicas'], {
          state: { aviso: id === null ? 'Clínica creada.' : 'Cambios guardados.' },
        }),
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeHttp(err));
      },
    });
  }
}
