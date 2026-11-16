import { Component, input, signal, computed } from '@angular/core';
export function imagenSegura(url: string | null | undefined): string | null {
  if (!url) return null;
  try {
    const u = new URL(url);
    return ['http:', 'https:'].includes(u.protocol) ? u.href : null;
  } catch {
    return null;
  }
}
@Component({
  selector: 'app-foto-producto',
  template: `@if (fuente(); as src) {
      <img
        [src]="src"
        [alt]="nombre()"
        loading="lazy"
        referrerpolicy="no-referrer"
        (error)="fallida.set(src)"
      />
    } @else {
      <span
        class="placeholder"
        role="img"
        [attr.aria-label]="nombre() + ': imagen no disponible'"
        >{{ nombre().charAt(0) }}</span
      >
    }`,
  styles:
    ':host{display:block;width:100%;height:100%;min-height:64px;background:radial-gradient(ellipse at bottom,#76603e44,#1e211a);overflow:hidden}img{width:100%;height:100%;object-fit:cover;display:block}.placeholder{display:grid;place-items:center;width:100%;height:100%;min-height:90px;color:#c9b38b;font:2rem Georgia,serif}',
})
export class FotoProducto {
  readonly url = input<string | null | undefined>(null);
  readonly nombre = input('Producto');
  readonly fallida = signal('');
  readonly fuente = computed(() => {
    const u = imagenSegura(this.url());
    return u && u !== this.fallida() ? u : null;
  });
}
