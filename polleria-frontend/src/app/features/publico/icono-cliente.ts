import { Component, input } from '@angular/core';
const trazos: Record<string, string> = {
  buscar: 'M21 21l-5-5M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0',
  menu: 'M4 6h16M4 12h16M4 18h16',
  inicio: 'm3 11 9-8 9 8M5 10v11h5v-7h4v7h5V10',
  carta: 'M3 4h7l2 2 2-2h7v16h-7l-2 2-2-2H3ZM12 6v16',
  carrito: 'M2 3h3l3 13h12l2-10H6M9 21h.01M19 21h.01',
  cuenta: 'M20 21v-2a7 7 0 0 0-14 0v2M17 6a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
  pedido: 'M7 3H4v18l4-2 4 2 4-2 4 2V3h-3M9 3h6v4H9ZM8 11h8M8 15h5',
  volver: 'm15 5-7 7 7 7',
  flecha: 'M4 12h16m-6-6 6 6-6 6',
  cerrar: 'm6 6 12 12M18 6 6 18',
  borrar: 'M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7M14 10v7',
  ubicacion: 'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1 1 16 0ZM15 10a3 3 0 1 1-6 0 3 3 0 0 1 6 0',
  check: 'm5 12 4 4 10-11',
  reloj: 'M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0ZM12 6v6l4 2',
  tarjeta: 'M2 5h20v14H2ZM2 9h20M5 15h5',
  efectivo: 'M2 5h20v14H2ZM15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0M5 12h.01M19 12h.01',
  bolsa: 'M5 6h14l2 15H3ZM8 8V5a4 4 0 0 1 8 0v3',
  mesa: 'M4 8h16M6 8l-2 13M18 8l2 13M9 4h6',
};
@Component({
  selector: 'app-icono-cliente',
  template:
    '<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path [attr.d]="trazos[nombre()] || trazos[\'carta\']" /></svg>',
  styles:
    ':host{display:inline-flex;width:1.4em;height:1.4em;flex-shrink:0;vertical-align:middle}svg{width:100%;height:100%;fill:none;stroke:currentColor;stroke-width:1.6;stroke-linecap:round;stroke-linejoin:round}',
})
export class IconoCliente {
  readonly nombre = input('carta');
  readonly trazos = trazos;
}
