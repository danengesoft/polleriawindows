import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppNavigation } from '../../shared/navigation/app-navigation';
@Component({
  selector: 'app-sin-permiso',
  imports: [RouterLink, AppNavigation],
  template: `<main class="container auth-page">
    <app-navigation />
    <section class="access-message">
      <p class="eyebrow">Acceso restringido</p>
      <h1>Acceso denegado</h1>
      <p>No tienes permiso para acceder a esta pantalla.</p>
      <a class="action" routerLink="/">Volver al inicio</a>
    </section>
  </main>`,
  styles:
    '.auth-page{padding-block:2rem}.access-message{max-width:40rem;padding:2rem;margin:3rem auto;background:var(--surface);border:1px solid var(--line);border-radius:var(--radius)}',
})
export class SinPermiso {}
