import { Component, computed, input } from '@angular/core';
import { etiqueta } from '../features/pedidos/pedido.model';
@Component({
  selector: 'app-estado',
  template: `<span class="badge" [attr.data-tono]="tono()">{{ etiqueta(valor()) }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 0.25rem 0.6rem;
      border-radius: 0.3rem;
      font-size: 0.78rem;
      font-weight: 650;
      line-height: 1.5;
      color: var(--ink);
      background: var(--surface-soft);
      white-space: normal;
    }
    .badge[data-tono='success'] {
      color: var(--success);
      background: var(--success-soft);
    }
    .badge[data-tono='warning'] {
      color: var(--warning);
      background: var(--warning-soft);
    }
    .badge[data-tono='error'] {
      color: var(--error);
      background: var(--error-soft);
    }
    .badge[data-tono='info'] {
      color: var(--info);
      background: var(--info-soft);
    }
  `,
})
export class EstadoBadge {
  readonly valor = input.required<string>();
  readonly etiqueta = etiqueta;
  readonly tono = computed(() => {
    const v = this.valor();
    return ['ENTREGADO', 'PAGADO', 'LISTO'].includes(v)
      ? 'success'
      : ['CANCELADO', 'ANULADO', 'NO_ENTREGADO'].includes(v)
        ? 'error'
        : ['PENDIENTE', 'ASIGNADO'].includes(v)
          ? 'warning'
          : 'info';
  });
}
