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
import { ClienteService } from './cliente-service';
import { ClienteRequest } from './cliente.model';

@Component({
  selector: 'app-cliente-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
  ],
  templateUrl: './cliente-form.html',
})
export class ClienteForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClienteService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private correoOriginal = '';
  readonly id = signal<number | null>(null);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly errorCarga = signal(false);
  readonly error = signal('');
  readonly validacion = mensajeValidacion;
  readonly form = this.fb.nonNullable.group({
    nombres: ['', [textoObligatorio, Validators.maxLength(100)]],
    apellidos: ['', [textoObligatorio, Validators.maxLength(100)]],
    telefono: [
      '',
      [textoObligatorio, Validators.maxLength(12), Validators.pattern(/^(\+51)?[0-9]{7,9}$/)],
    ],
    correo: ['', [Validators.maxLength(254), Validators.email, (c: any) => !c.value || c.value.trim().toLowerCase() === this.correoOriginal || /^[^@\s]+@gmail\.com$/i.test(c.value.trim()) ? null : { gmail: true }]],
    direccionPrincipal: ['', [Validators.maxLength(300)]],
    referencia: ['', [Validators.maxLength(300)]],
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
            this.correoOriginal = (data.correo ?? '').trim().toLowerCase();
            this.form.patchValue({
              ...data,
              correo: data.correo ?? '',
              direccionPrincipal: data.direccionPrincipal ?? '',
              referencia: data.referencia ?? '',
            });
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
    this.form.controls.nombres.setValue(this.form.controls.nombres.value.trim());
    this.form.controls.apellidos.setValue(this.form.controls.apellidos.value.trim());
    this.form.controls.telefono.setValue(this.form.controls.telefono.value.trim());
    this.form.controls.correo.setValue(this.form.controls.correo.value.trim());
    this.error.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const data: ClienteRequest = {
      nombres: value.nombres.trim(),
      apellidos: value.apellidos.trim(),
      telefono: value.telefono.trim(),
      correo: value.correo.trim() || null,
      direccionPrincipal: value.direccionPrincipal.trim() || null,
      referencia: value.referencia.trim() || null,
      activo: value.activo,
    };
    this.guardando.set(true);
    this.form.disable();
    const id = this.id();
    const request = id === null ? this.service.crear(data) : this.service.actualizar(id, data);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () =>
        this.router.navigate(['/interno/clientes'], {
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
