import { AccesoModalService } from './acceso-dialog';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { ClienteShell } from '../../core/layout/publico/cliente-shell';
import { IconoCliente } from './icono-cliente';
@Component({
  selector: 'app-acceso',
  imports: [ClienteShell, IconoCliente, RouterLink],
  template: `<app-cliente-shell [amplio]="true"
    ><section class="access-page">
      <div class="access-photo" aria-hidden="true"></div>
      <div class="access-copy">
        <p class="eyebrow">Buena comida, mejores momentos</p>
        <h1>LA BRAVA</h1>
        <h2>Bienvenido</h2>
        <p>Disfruta de nuestros pollos y parrillas, donde estés.</p>
        <div class="access-actions">
          <button type="button" (click)="acceso.abrir()">
            <app-icono-cliente nombre="cuenta" />Iniciar sesión<app-icono-cliente
              nombre="flecha"
            /></button
          ><button type="button" class="secondary" (click)="acceso.abrir()">
            <app-icono-cliente nombre="pedido" />Registrarme como cliente<app-icono-cliente
              nombre="flecha"
            />
          </button>
        </div>
        <p class="access-note">
          Primera visita: regístrate y luego inicia sesión. Tu cuenta te permite consultar tus
          pedidos.
        </p>
        <a class="access-link" routerLink="/inicio">Descubrir La Brava</a>
        <p class="access-note">
          Los trabajadores utilizan únicamente su cuenta previamente autorizada.
        </p>
        @if (auth.error()) {
          <p role="alert">{{ auth.error() }}</p>
        }
      </div>
    </section></app-cliente-shell
  >`,
  styleUrl: './acceso.css',
})
export class Acceso {
  readonly acceso = inject(AccesoModalService);
  readonly auth = inject(AuthService);
}
