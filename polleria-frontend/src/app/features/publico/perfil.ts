import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ClientePublicoService } from './cliente-publico-service';
import { mensajeHttp } from '../../shared/http-error';
@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule, RouterLink],
  styleUrl: './publico.css',
  templateUrl: './perfil.html',
})
export class Perfil {
  private readonly api = inject(ClientePublicoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  readonly existe = signal(false);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly aviso = signal('');
  readonly listo = signal(false);
  readonly form = this.fb.nonNullable.group({
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(100)]],
    telefono: ['', [Validators.required, Validators.pattern(/^(\+51)?[0-9]{7,9}$/)]],
    correo: ['', [Validators.email, Validators.maxLength(254)]],
    direccionPrincipal: ['', [Validators.maxLength(300)]],
    referencia: ['', [Validators.maxLength(300)]],
  });
  constructor() {
    this.cargar();
  }
  cargar() {
    this.cargando.set(true);
    this.error.set('');
    this.api.perfil().subscribe({
      next: (p) => {
        this.form.patchValue({
          ...p,
          correo: p.correo ?? '',
          direccionPrincipal: p.direccionPrincipal ?? '',
          referencia: p.referencia ?? '',
        });
        this.existe.set(true);
        this.listo.set(true);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        if (e instanceof HttpErrorResponse && e.status === 404) {
          this.listo.set(true);
        } else this.error.set(mensajeHttp(e));
      },
    });
  }
  guardar() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.guardando() || !this.listo()) return;
    this.guardando.set(true);
    this.error.set('');
    const r = this.form.getRawValue();
    this.api
      .guardarPerfil(
        {
          ...r,
          correo: r.correo.trim() || null,
          direccionPrincipal: r.direccionPrincipal.trim() || null,
          referencia: r.referencia.trim() || null,
        },
        this.existe(),
      )
      .subscribe({
        next: () => {
          this.existe.set(true);
          this.guardando.set(false);
          this.aviso.set('Perfil guardado.');
          if (this.route.snapshot.queryParamMap.get('continuar') === 'checkout')
            void this.router.navigateByUrl('/checkout');
        },
        error: (e) => {
          this.guardando.set(false);
          this.error.set(mensajeHttp(e));
        },
      });
  }
}
