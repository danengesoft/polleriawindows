import { Component, input } from '@angular/core';
import { CurrencyPipe, registerLocaleData } from '@angular/common';
import esPE from '@angular/common/locales/es-PE';
registerLocaleData(esPE);
export interface Columna {
  nombre: string;
  formato?: 'moneda';
}
export type Celda = string | number;
export interface TablaReporte {
  cargando: boolean;
  error: string;
  filas: Celda[][];
}
@Component({
  selector: 'app-reporte-tabla',
  imports: [CurrencyPipe],
  template: ` <section [attr.aria-busy]="datos().cargando">
    <h3>{{ titulo() }}</h3>
    @if (nota()) {
      <p>{{ nota() }}</p>
    }
    @if (datos().cargando) {
      <p role="status">Cargando…</p>
    } @else if (datos().error) {
      <p role="alert">{{ datos().error }}</p>
    } @else if (!datos().filas.length) {
      <p>Sin datos para el rango seleccionado.</p>
    } @else {
      <div class="table-scroll" tabindex="0" [attr.aria-label]="titulo()">
        <table>
          <caption>
            {{
              titulo()
            }}
          </caption>
          <thead>
            <tr>
              @for (c of columnas(); track $index) {
                <th scope="col">{{ c.nombre }}</th>
              }
            </tr>
          </thead>
          <tbody>
            @for (f of datos().filas; track $index) {
              <tr>
                @for (v of f; track $index) {
                  <td>
                    @switch (columnas()[$index].formato) {
                      @case ('moneda') {
                        {{ asNumero(v) | currency: 'PEN' : 'S/' : '1.2-2' : 'es-PE' }}
                      }
                      @default {
                        {{ v }}
                      }
                    }
                  </td>
                }
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  </section>`,
  styles: [
    `
      :host {
        display: block;
        min-width: 0;
      }
      section {
        margin-block: 1.5rem;
      }
      h3 {
        margin-bottom: 0.4rem;
      }
      p {
        color: var(--muted);
      }
      .table-scroll {
        max-width: 100%;
        overflow-x: auto;
        border: 1px solid var(--line);
        border-radius: 0.4rem;
      }
      table {
        border-collapse: collapse;
        width: 100%;
        font-size: 0.95rem;
      }
      caption {
        text-align: left;
        padding: 0.65rem;
        background: var(--surface-soft);
        font-weight: 600;
      }
      th,
      td {
        text-align: left;
        padding: 0.75rem;
        border-bottom: 1px solid var(--line);
        white-space: nowrap;
        font-variant-numeric: tabular-nums;
      }
      th {
        background: var(--surface-soft);
      }
    `,
  ],
})
export class ReporteTabla {
  readonly titulo = input.required<string>();
  readonly nota = input('');
  readonly columnas = input.required<Columna[]>();
  readonly datos = input.required<TablaReporte>();
  asNumero(v: Celda) {
    return Number(v);
  }
}
