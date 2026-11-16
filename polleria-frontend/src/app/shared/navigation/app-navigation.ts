import { Component, inject, input, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { CarritoService } from '../../features/publico/carrito-service';
@Component({
  selector: 'app-navigation',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav [class.internal-session]="interno()" aria-label="Navegación principal" (keydown.escape)="cerrarMenu(menuButton)">
      @if (!interno()) {
        <a
          (click)="abierto.set(false)"
          routerLink="/"
          routerLinkActive="active"
          [routerLinkActiveOptions]="{ exact: true }"
          ariaCurrentWhenActive="page"
          >Inicio</a
        >
        <a
          (click)="abierto.set(false)"
          routerLink="/catalogo"
          routerLinkActive="active"
          ariaCurrentWhenActive="page"
          >Catálogo</a
        >
        <a
          class="cart-link"
          (click)="abierto.set(false)"
          routerLink="/carrito"
          routerLinkActive="active"
          ariaCurrentWhenActive="page"
          >Carrito ({{ carrito.unidades() }})</a
        >
      }

      <button
        class="secondary menu-toggle"
        #menuButton
        type="button"
        [attr.aria-expanded]="abierto()"
        [attr.aria-controls]="interno() ? 'sesion-interna' : 'sesion-publica'"
        (click)="abierto.set(!abierto())"
      >
        Menú
      </button>
      <div
        class="nav-extra"
        [class.abierto]="abierto()"
        [id]="interno() ? 'sesion-interna' : 'sesion-publica'"
        (click)="abierto.set(false)"
      >
        @if (!interno() && auth.tieneRol('CLIENTE')) {
          <a routerLink="/mis-pedidos" routerLinkActive="active" ariaCurrentWhenActive="page"
            >Mis pedidos</a
          ><a routerLink="/perfil" routerLinkActive="active" ariaCurrentWhenActive="page">Perfil</a>
        }
        @if (auth.usuario()) {
          @if (
            !interno() && auth.tieneRol('ADMINISTRADOR', 'CAJERO', 'OPERACIONES', 'MESERO')
          ) {
            <a routerLink="/interno" routerLinkActive="active">Portal interno</a>
          }
          <span class="identity"
            >{{ auth.usuario()?.nombre }}
            @if (interno()) {
              <small>{{ auth.roles().join(' · ') }}</small>
            }
          </span>
          <button type="button" class="secondary" (click)="auth.cerrarSesion()">
            Cerrar sesión
          </button>
        } @else {
          <button type="button" (click)="auth.iniciarLogin('/')">Iniciar sesión</button>
        }
      </div>
    </nav>
    @if (auth.error()) {
      <p role="alert">{{ auth.error() }}</p>
    }
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }
    nav,
    .nav-extra {
      display: flex;
      align-items: center;
      gap: 0.3rem;
      flex-wrap: wrap;
    }
    a {
      padding: 0.65rem 0.7rem;
      min-height: 44px;
      color: var(--muted);
      text-decoration: none;
      font-size: 0.9rem;
      border-radius: var(--radius);
    }
    a:hover,
    a.active {
      color: var(--ink);
      background: var(--surface-soft);
    }
    .cart-link {
      font-weight: 650;
      color: var(--accent);
    }
    button {
      font-size: 0.85rem;
      margin: 0;
    }
    .identity {
      font-size: 0.8rem;
      max-width: 11rem;
      overflow-wrap: anywhere;
      padding-inline: 0.5rem;
    }
    small {
      display: block;
      color: var(--muted);
      font-size: 0.65rem;
    }
    .menu-toggle {
      display: none;
    }
    @media (max-width: 900px) {
      :host {
        width: 100%;
      }
      nav {
        width: 100%;
        gap: 0.15rem;
      }
      .menu-toggle {
        display: inline-flex;
        margin-left: auto;
      }
      .nav-extra {
        display: none;
        width: 100%;
        padding-top: 0.75rem;
        margin-top: 0.5rem;
        border-top: 1px solid var(--line);
        gap: 0.5rem;
      }
      .nav-extra.abierto {
        display: flex;
      }
      .identity {
        margin-left: auto;
      }
      .nav-extra a {
        flex: 1;
      }
      a {
        padding-inline: 0.65rem;
      }
      .public-link {
        margin-right: auto;
      }
    }
    @media(max-width:1000px){
      .internal-session .menu-toggle{display:none}
      .internal-session,.internal-session .nav-extra{display:flex;flex-wrap:nowrap;width:100%;min-width:0;gap:.4rem}
      .internal-session .nav-extra{padding:0;margin:0;border:0}
      .internal-session .identity{margin:0;min-width:0;flex:1;font-size:.75rem;padding:0;max-width:none}
      .internal-session .identity small{font-size:.58rem}
      .internal-session .nav-extra>button{margin-left:auto;flex:none;padding:10px;font-size:.75rem;white-space:nowrap}
    }
  `,
})
export class AppNavigation {
  cerrarMenu(boton: HTMLButtonElement) {
    this.abierto.set(false);
    // Keep keyboard focus on the visible disclosure when its contents are hidden.
    if (boton.offsetWidth > 0) boton.focus();
  }
  readonly auth = inject(AuthService);
  readonly carrito = inject(CarritoService);
  readonly interno = input(false);
  readonly abierto = signal(false);
}
