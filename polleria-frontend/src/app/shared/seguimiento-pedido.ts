import { Component, computed, input } from '@angular/core';
import { EstadoPedido, TipoPedido, etiqueta } from '../features/pedidos/pedido.model';
import { EstadoBadge } from './estado-badge';
@Component({
  selector: 'app-seguimiento-pedido',
  imports: [EstadoBadge],
  template: `<section aria-label="Seguimiento del pedido" [class.horizontal]="horizontal()">
    <h3>Estado de tu pedido</h3>
    @if (estado() === 'CANCELADO') {
      <app-estado valor="CANCELADO" />
    } @else {
      <ol>
        @for (paso of pasos(); track paso) {
          <li
            [class.actual]="paso === estado()"
            [class.completo]="pasos().indexOf(paso) < pasos().indexOf(estado())"
            [attr.aria-current]="paso === estado() ? 'step' : null"
          >
            <span
              class="step-mark"
              aria-hidden="true"
              [textContent]="pasos().indexOf(paso) &lt; pasos().indexOf(estado()) ? '✓' : pasos().indexOf(paso)+1"
            ></span>
            <div>
              <strong>{{ etiqueta(paso) }}</strong>
              @if (paso === estado()) {
                <small>Estado actual</small>
              }
            </div>
          </li>
        }
      </ol>
    }
  </section>`,
  styles: `
    @media(min-width:700px){section.horizontal{padding:15px 0;border:0;background:transparent}.horizontal h3{display:none}.horizontal ol{grid-template-columns:repeat(4,minmax(0,1fr))}.horizontal ol:has(li:nth-child(3):last-child){grid-template-columns:repeat(3,minmax(0,1fr))}.horizontal li{flex-direction:column;align-items:center;text-align:center;gap:12px;min-height:100px}.horizontal li:not(:last-child):before{height:2px;width:calc(100% - 55px);left:calc(50% + 28px);top:16px;bottom:auto}.horizontal li.completo:before{background:var(--accent)}}
    section {
      margin: 24px 0;
      padding: 20px;
      border: 1px solid var(--line);
      border-radius: 10px;
      background: var(--surface);
    }
    h3 {
      font:
        1.3rem Georgia,
        serif;
      color: var(--ink);
      margin: 0 0 20px;
    }
    ol {
      padding: 0;
      margin: 0;
      list-style: none;
      display: grid;
      gap: 0;
    }
    li {
      display: flex;
      align-items: start;
      gap: 14px;
      color: var(--muted);
      min-height: 62px;
      position: relative;
    }
    li:not(:last-child):before {
      content: '';
      position: absolute;
      width: 1px;
      left: 15px;
      top: 30px;
      bottom: 0;
      background: var(--line);
    }
    .step-mark {
      width: 32px;
      height: 32px;
      border: 1px solid var(--line);
      border-radius: 50%;
      display: grid;
      place-items: center;
      flex-shrink: 0;
      font-size: 0.8rem;
      background: var(--surface-soft);
      position: relative;
      z-index: 1;
    }
    li.actual {
      color: var(--ink);
    }
    li.actual .step-mark {
      background: var(--accent);
      border-color: var(--accent);
      color: white;
    }
    li.completo .step-mark {
      background: #4c7534;
      border-color: #658948;
      color: white;
    }
    li strong {
      display: block;
      font-size: 0.9rem;
      font-weight: 500;
      padding-top: 4px;
    }
    li small {
      display: block;
      color: var(--muted);
      font-size: 0.72rem;
      margin-top: 5px;
    }
    li:last-child {
      min-height: 32px;
    }
  `,
})
export class SeguimientoPedido {
  readonly horizontal=input(false);
  readonly estado = input.required<EstadoPedido>();
  readonly tipo = input.required<TipoPedido>();
  readonly etiqueta = etiqueta;
  readonly pasos = computed<EstadoPedido[]>(() => [
    'PENDIENTE',
    'LISTO',
    ...(this.tipo() === 'DELIVERY' ? ['EN_CAMINO' as const] : []),
    'ENTREGADO',
  ]);
}
