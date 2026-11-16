import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ConexionService, ResultadoConexion } from '../services/conexion-service';

@Component({
  selector: 'app-conexion',
  template: `
    <section class="intro">
      <p class="eyebrow">Diagnóstico de desarrollo</p>
      <h1>Conexión con el servidor</h1>
      <p>Comprueba que la aplicación puede comunicarse con el backend.</p>
      <button type="button" (click)="comprobar()" [disabled]="estado() === 'comprobando'">
        {{ estado() === 'comprobando' ? 'Comprobando…' : 'Comprobar conexión' }}
      </button>
      <div aria-live="polite" class="connection-result">
        @if (estado() === 'correcta') {
          <p class="success">Conexión correcta · HTTP {{ resultado()?.codigoHttp }}</p>
          <p class="trace">Trace ID: {{ resultado()?.traceId }}</p>
        } @else if (estado() === 'error') {
          <p role="alert">
            No se pudo conectar con el servidor. Comprueba que el backend esté disponible.
          </p>
        }
      </div>
    </section>
  `,
  styles: `
    .connection-result {
      margin-top: 1.25rem;
    }
    .success {
      color: var(--success);
      font-weight: 600;
    }
    .trace {
      color: var(--muted);
      font-size: 0.85rem;
      overflow-wrap: anywhere;
    }
  `,
})
export class Conexion {
  private readonly conexion = inject(ConexionService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly estado = signal<'inicial' | 'comprobando' | 'correcta' | 'error'>('inicial');
  protected readonly resultado = signal<ResultadoConexion | null>(null);

  protected comprobar() {
    this.estado.set('comprobando');
    this.resultado.set(null);
    this.conexion
      .comprobar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resultado) => {
          this.resultado.set(resultado);
          this.estado.set('correcta');
        },
        error: () => this.estado.set('error'),
      });
  }
}
