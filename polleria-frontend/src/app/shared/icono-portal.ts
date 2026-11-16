import { Component, input } from '@angular/core';
@Component({ selector: 'app-icono-portal', template: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path [attr.d]="trazos[nombre()]||trazos['pedido']"/></svg>`, styles: ':host{display:inline-flex;flex:0 0 22px;width:22px;height:22px}svg{width:100%;height:100%}' })
export class IconoPortal {
    readonly nombre = input('pedido');
    readonly trazos: Record<string, string> = {
        cocina: 'M6 13a4 4 0 0 1-2-7 4 4 0 0 1 7-3 4 4 0 0 1 7 3 4 4 0 0 1 0 7v7H6z M6 16h12',
        mesa: 'M5 11h14l2 10M5 11 3 21M6 11V5h12v6M8 5V2m8 3V2',
        categoria: 'M3 3h8l10 10-8 8L3 11zM7 7h.01',
        producto: 'm3 7 9-5 9 5v10l-9 5-9-5zM3 7l9 5 9-5M12 12v10',
        cliente: 'M16 21v-3a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v3M9 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8M22 21v-3a4 4 0 0 0-3-4M16 2a4 4 0 0 1 0 8',
        pedido: 'M5 3h14v18H5zM8 7h8M8 11h8M8 15h5',
        reporte: 'M4 21V11M10 21V3M16 21V8M22 21V14',
        caja: 'M3 5h18v14H3zM3 10h18M7 15h3',
    };
}
