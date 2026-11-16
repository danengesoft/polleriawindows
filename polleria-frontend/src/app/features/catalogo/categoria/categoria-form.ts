import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { mensajeHttp } from '../../../shared/http-error';
import { mensajeValidacion, textoObligatorio } from '../../../shared/form-validation';
import { CategoriaService } from './categoria-service';
import { CategoriaRequest } from './categoria.model';

@Component({
  selector: 'app-categoria-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
  ],
  templateUrl: './categoria-form.html',
})
export class CategoriaForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CategoriaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly id = signal<number | null>(null);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly errorCarga = signal(false);
  readonly error = signal('');
  readonly validacion = mensajeValidacion;
  readonly form = this.fb.nonNullable.group({
    nombre: ['', [textoObligatorio, Validators.maxLength(80)]],
    descripcion: ['', [Validators.maxLength(500)]],
    activo: [true, []],
  });

  constructor() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param !== null) {
      const id = Number(param);
      if (!Number.isSafeInteger(id) || id <= 0) {
        this.errorCarga.set(true);
        this.error.set('Identificador inválido.');
        return;
      }
      this.id.set(id);
      this.loading.set(true);
      this.service
        .obtener(id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (data) => {
            this.form.patchValue({ ...data, descripcion: data.descripcion ?? '' });
            this.loading.set(false);
          },
          error: (err) => {
            this.loading.set(false);
            this.errorCarga.set(true);
            this.error.set(mensajeHttp(err));
          },
        });
    }
  }
  guardar() {
    if (this.loading() || this.guardando() || this.errorCarga()) return;
    this.form.controls.nombre.setValue(this.form.controls.nombre.value.trim());
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const data: CategoriaRequest = {
      nombre: value.nombre.trim(),
      descripcion: value.descripcion.trim() || null,
      activo: value.activo,
    };
    this.guardando.set(true);
    this.form.disable();
    const id = this.id();
    const request = id === null ? this.service.crear(data) : this.service.actualizar(id, data);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () =>
        this.router.navigate(['/interno/categorias'], {
          state: { aviso: id === null ? 'Registro creado.' : 'Cambios guardados.' },
        }),
      error: (err) => {
        this.guardando.set(false);
        this.form.enable();
        this.error.set(mensajeHttp(err));
      },
    });
  }
}
